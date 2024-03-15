// package ui;

// import java.util.Scanner;
// import ServerFacade;

// import exception.ResponseException;
// public class PreLoginClient {
    
//     private Scanner input = new Scanner(System.in);
//     private Status status = Status.LoggedOut;
//     private ServerFacade server = new ServerFacade("http://localhost:4567");

//     public void quit() {
//         System.out.println("Goodbye!");
//         System.exit(0);
//     }

//     public void login() {
//         System.out.println("Please enter your username and password.");
//         System.out.println("Username: ");
//         String username = input.nextLine();
//         System.out.println("Password: ");
//         String password = input.nextLine();
//         try {
//             var token = server.loginUser(username, password);
//             status = Status.LoggedIn;
//             System.out.println("You are now logged in.");
//             System.out.println("Your token is: " + token);
//         } catch (ResponseException ex) {
//             System.out.println("Login failed: " + ex.getMessage());
//         }
//     }

// }
