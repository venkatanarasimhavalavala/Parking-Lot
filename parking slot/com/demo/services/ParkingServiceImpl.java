package com.demo.services;

import com.demo.dto.*;
import com.demo.dto.parkingSpot.*;
import com.demo.dto.parkingSpot.spotDecorator.*;
import com.demo.dto.vehicle.*;
import com.demo.enums.*;
import com.demo.exceptions.*;
import com.demo.interfaces.*;
import com.demo.interfaces.Observer;
import com.demo.parkingStrategy.*;

import java.util.*;

public class ParkingServiceImpl implements ParkingService {

    private Strategy parkingStrategy;
    private ParkingLot parkingLot;
    private DisplayService displayService;
    private List<Observer> observers;
    private Map<Integer, ParkingTicket> ticketRegistry; // Ticket Registry

    public ParkingServiceImpl(Strategy parkingStrategy) {
        this.parkingStrategy = parkingStrategy;
        this.parkingLot = ParkingLot.getInstance();
        this.displayService = new DisplayServiceImpl();
        this.observers = new ArrayList<>();
        this.ticketRegistry = new HashMap<>(); // Initialize the ticket registry
    }

    @Override
    public ParkingTicket entry(Vehicle vehicle) {
        ParkingSpotEnum parkingSpotEnum = vehicle.getParkingSpotEnum();
        List<ParkingSpot> freeParkingSpots = parkingLot.getFreeParkingSpots().get(parkingSpotEnum);
        List<ParkingSpot> occupiedParkingSpots = parkingLot.getOccupiedParkingSpots().get(parkingSpotEnum);

        try {
            ParkingSpot parkingSpot = parkingStrategy.findParkingSpot(parkingSpotEnum);

            synchronized (parkingSpot) {
                if (parkingSpot.isFree()) {
                    parkingSpot.setFree(false);
                    freeParkingSpots.remove(parkingSpot);
                    occupiedParkingSpots.add(parkingSpot);

                    ParkingTicket parkingTicket = new ParkingTicket(vehicle, parkingSpot);
                    ticketRegistry.put(parkingTicket.getId(), parkingTicket); // Add to ticket registry

                    ParkingEvent parkingEvent = new ParkingEvent(ParkingEventType.EnTRY, parkingSpotEnum);
                    notifyAllObservers(parkingEvent);

                    return parkingTicket;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to allocate parking spot: " + e.getMessage(), e);
        }

        throw new RuntimeException("No parking spot available for vehicle type: " + parkingSpotEnum);
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void notifyAllObservers(ParkingEvent parkingEvent) {
        for (Observer observer : observers) {
            observer.update(parkingEvent);
        }
    }

    private void addParkingSpotInFreeList(List<ParkingSpot> parkingSpots, ParkingSpot parkingSpot) {
        parkingSpots.add(parkingSpot);
    }

    public void addWash(ParkingTicket parkingTicket) {
        parkingTicket.setParkingSpot(new Wash(parkingTicket.getParkingSpot()));
    }

    @Override
    public int exit(ParkingTicket parkingTicket, Vehicle vehicle) throws InvalidTicketException {
        if (parkingTicket.getVehicle().equals(vehicle)) {
            ParkingSpot parkingSpot = parkingTicket.getParkingSpot();
            int amount = parkingSpot.getAmount();
            parkingSpot.setFree(true);

            parkingLot.getOccupiedParkingSpots().get(vehicle.getParkingSpotEnum()).remove(parkingSpot);
            addParkingSpotInFreeList(parkingLot.getFreeParkingSpots().get(vehicle.getParkingSpotEnum()), parkingSpot);

            ParkingEvent parkingEvent = new ParkingEvent(ParkingEventType.EXIT, vehicle.getParkingSpotEnum());
            notifyAllObservers(parkingEvent);

            ticketRegistry.remove(parkingTicket.getId()); // Remove ticket from registry
            return amount;
        } else {
            throw new InvalidTicketException("This is an invalid ticket");
        }
    }

    public ParkingTicket get1TicketById(int ticketId) {
        if (!ticketRegistry.containsKey(ticketId)) {
            throw new RuntimeException("Ticket ID not found: " + ticketId);
        }
        return ticketRegistry.get(ticketId); // Fetch ticket from registry
    }
}
