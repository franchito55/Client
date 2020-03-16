package model;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ComUtils comUtils;
        String userInput;
        Game game;

        String nomMaquina;
        int numPort, id;

        InetAddress maquinaServidora;
        Socket socket = null;

        if(args.length >= 1) {
            if (args[0].equals("-h")) {
                System.out.println("Us: java Client <ip_servidor> <port>");
                System.exit(1);
            }

            if (args.length != 2)
            {
                System.out.println("Us: java Client <ip_servidor> <port>");
                System.exit(1);
            }
        }else{
            System.out.println("Us: java Client <ip_servidor> <port>");
            System.exit(1);
        }

        nomMaquina = args[0];
        numPort    = Integer.parseInt(args[1]);

        try
        {
            /* Obtenim la IP de la maquina servidora */
            maquinaServidora = InetAddress.getByName(nomMaquina);

            /* Obrim una connexio amb el servidor */
            socket = new Socket(maquinaServidora, numPort);

            /* Obrim un flux d'entrada/sortida amb el servidor */
            comUtils = new ComUtils(socket);

            do{

                System.out.println("Hola! Escribe STRT + ID para empezar.");
                userInput = scanner.nextLine();

                switch (userInput.substring(0, 4)){
                    case "EXIT":
                        System.out.println("Gracies per jugar!");
                        socket.close();
                        System.exit(1);
                        break;

                    case "STRT":
                        comUtils.write_string("STRT");
                        comUtils.write_byte(' ');
                        id = Integer.parseInt(userInput.substring(5, userInput.length()));
                        comUtils.write_int32(id);
                        game = new Game(socket, comUtils, id);
                        game.startGame();

                        break;

                    default:
                        System.out.println("Por favor, introduce un comando válido.");
                }
            }while(!userInput.equals("EXIT"));

        }
        catch (IOException e)
        {
            System.out.println("Hi ha hagut un error al programa.");
        }
        finally
        {
            try {
                if(socket != null) socket.close();
            }
            catch (IOException ex) {
                System.out.println("Hi ha hagut un error a la connexio amb el servidor.");
            } // fi del catch
        }
    }

}
