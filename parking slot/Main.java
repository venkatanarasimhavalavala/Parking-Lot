import com.demo.dto.*;
import com.demo.dto.parkingSpot.*;
import com.demo.dto.vehicle.*;
import com.demo.enums.*;
import com.demo.exceptions.*;
import com.demo.interfaces.*;
import com.demo.parkingStrategy.*;
import com.demo.services.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ParkingLot parkingLot = ParkingLot.getInstance();
        ParkingSpotService parkingSpotService = new ParkingSpotServiceImpl();
        ParkingServiceImpl parkingLotService = new ParkingServiceImpl(new FarthestFirstParkingStrategy());
        PaymentService paymentService = new PaymentServiceImpl();
        Scanner scanner = new Scanner(System.in);

        // Initialize parking spots
        parkingSpotService.create(ParkingSpotEnum.COMPACT, 0);
        parkingSpotService.create(ParkingSpotEnum.COMPACT, 0);
        parkingSpotService.create(ParkingSpotEnum.LARGE, 0);
        parkingSpotService.create(ParkingSpotEnum.LARGE, 0);
        parkingSpotService.create(ParkingSpotEnum.MINI, 0);
        parkingSpotService.create(ParkingSpotEnum.MINI, 0);

        boolean exitProgram = false;

        while (!exitProgram) {
            System.out.println("\nWelcome to the Parking Lot Management System");
            System.out.println("1. Park a Vehicle");
            System.out.println("2. Add Car Wash Service");
            System.out.println("3. Exit Parking Lot");
            System.out.println("4. Exit Program");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1: // Park a Vehicle
                    System.out.print("Enter vehicle type (Car/Truck): ");
                    String vehicleType = scanner.nextLine();
                    Vehicle vehicle;

                    if (vehicleType.equalsIgnoreCase("Car")) {
                        vehicle = new Car();
                    } else if (vehicleType.equalsIgnoreCase("Truck")) {
                        vehicle = new Truck();
                    } else {
                        System.out.println("Invalid vehicle type. Please try again.");
                        break;
                    }

                    ParkingTicket ticket = parkingLotService.entry(vehicle);
                    if (ticket != null) {
                        System.out.println("Vehicle parked successfully!");
                        System.out.println("Your Parking Ticket ID: " + ticket.getId());
                        System.out.println("Parking Spot Details: " + ticket.getParkingSpot());
                    } else {
                        System.out.println("Parking failed. No available spots.");
                    }
                    break;

                case 2: // Add Car Wash Service
                    System.out.print("Enter Parking Ticket ID to add car wash: ");
                    int ticketIdForWash = scanner.nextInt();

                    try {
                        ParkingTicket ticketForWash = parkingLotService.getTicketById(ticketIdForWash);
                        parkingLotService.addWash(ticketForWash);
                        System.out.println("Car wash service added to ticket ID: " + ticketIdForWash);
                    } catch (Exception e) {
                        System.out.println("Invalid ticket ID. Please try again.");
                    }
                    break;

                case 3: // Exit Parking Lot
                    System.out.print("Enter Parking Ticket ID to exit: ");
                    int ticketIdToExit = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    System.out.print("Enter vehicle type (Car/Truck): ");
                    String exitingVehicleType = scanner.nextLine();

                    Vehicle exitingVehicle;
                    if (exitingVehicleType.equalsIgnoreCase("Car")) {
                        exitingVehicle = new Car();
                    } else if (exitingVehicleType.equalsIgnoreCase("Truck")) {
                        exitingVehicle = new Truck();
                    } else {
                        System.out.println("Invalid vehicle type. Please try again.");
                        break;
                    }

                    try {
                        ParkingTicket ticketToExit = parkingLotService.getTicketById(ticketIdToExit);
                        int cost = parkingLotService.exit(ticketToExit, exitingVehicle);
                        System.out.println("Parking cost: " + cost);

                        System.out.print("Pay by cash? (yes/no): ");
                        String paymentChoice = scanner.nextLine();
                        if (paymentChoice.equalsIgnoreCase("yes")) {
                            paymentService.acceptCash(cost);
                            System.out.println("Payment successful. Thank you!");
                        } else {
                            System.out.println("Payment not completed. Please visit the counter.");
                        }
                    } catch (InvalidTicketException e) {
                        System.out.println("Error: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("Invalid ticket ID. Please try again.");
                    }
                    break;

                case 4: // Exit Program
                    exitProgram = true;
                    System.out.println("Exiting program. Have a great day!");
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
    }
}
