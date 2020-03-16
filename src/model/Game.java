package model;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Game {
    private Socket socket;
    private boolean hasShip = false, hasCaptain = false, hasCrew = false, validChoice = false;
    private int torn, tempInt, rondes = 0, id;
    private Scanner scanner = new Scanner(System.in);
    private ComUtils comUtils;
    private String userInput, response;

    public Game(Socket socket, ComUtils comUtils, int id){
        this.socket = socket;
        this.comUtils = comUtils;
        this.id = id;
    }

    public void startGame() throws IOException {
        if(comUtils.read_string().equals("CASH")){
            System.out.println("Tens " + comUtils.read_int32() + " de or. Vols apostar per jugar? (BETT per apostar)");
        }

        userInput = scanner.nextLine();
        switch(userInput.substring(0, 4)){
            case "BETT":
                comUtils.write_string("BETT");
                startPlaying();
                break;

            case "EXIT":
                System.exit(0);
                break;
        }
    }

    public void startPlaying() throws IOException {
        response = comUtils.read_string();

        if(!response.equals("LOOT")){
            System.out.println("Error en el protocol.");
            System.exit(0);
        }

        comUtils.read_bytes(1);
        System.out.println("Hi han " + comUtils.read_int32() + " monedes en joc.");

        response = comUtils.read_string();
        if(!response.equals("PLAY")){
            System.out.println("Error en el protocol.");
            System.exit(0);
        }

        comUtils.read_byte();

        torn = comUtils.read_int32();
        switch (torn){
            case 0:
                manualplay();
                autoplay();
                break;
            case 1:
                autoplay();
                manualplay();
                break;
        }

        if(!comUtils.read_string().equals("WINS")){
            System.out.println("Error en el protocolo.");
            System.exit(0);
        }
        comUtils.read_byte();
        switch (comUtils.read_byte()){
            case 0:
                System.out.println("Has ganado!");
                break;
            case 1:
                System.out.println("Ha ganado el servidor.");
                break;
            case 2:
                System.out.println("Ha habido un empate.");
                break;
        }
    }

    public void autoplay() throws IOException{
        rondes = 0;
        System.out.println("Le toca empezar al servidor");

        response = comUtils.read_string();
        while(response.equals("DICE")) {
            rondes++;
            System.out.println("Ronda " + rondes);
            System.out.println("Dados:");

            comUtils.read_byte();
            comUtils.read_int32();

            for (int i = 0; i < 5; i++) {
                comUtils.read_byte();
                System.out.println(comUtils.read_byte());
            }

            response = comUtils.read_string();
            comUtils.read_byte();
            comUtils.read_int32();
            comUtils.read_byte();
            if (!response.equals("TAKE")) {
                System.out.println("Error en el protocolo 0.");
                System.exit(0);
            }

            tempInt = comUtils.read_byte();
            for (int i = 0; i < tempInt; i++) {
                comUtils.read_byte();
            }

            response = comUtils.read_string();
        }

        System.out.println(response);
        if(!response.equals("PNTS")){
            System.out.println("Error en el protocolo 1.");
        }

        comUtils.read_byte();
        comUtils.read_int32();
        comUtils.read_byte();
        System.out.println("El servidor ha obtingut una puntuacio de " + comUtils.read_byte());
    }

    public void manualplay() throws IOException {
        System.out.println("Te toca empezar a ti.");
        rondes = 0;

        response = comUtils.read_string();
        while (response.equals("DICE")) {
            rondes += 1;
            System.out.println("Ronda " + rondes + ":");

            comUtils.read_byte();
            comUtils.read_int32();

            System.out.println("Dados:");

            for (int i = 0; i < 5; i++) {
                comUtils.read_byte();
                System.out.println(comUtils.read_byte());
            }

            System.out.println("Cuantos dados te quieres quedar?");

            userInput = scanner.nextLine();
            if(Integer.parseInt(userInput) == 0) {
                comUtils.write_string("PASS");
            }else if(Integer.parseInt(userInput) > 0){
                comUtils.write_string("TAKE");
                comUtils.write_byte(' ');
                comUtils.write_int32(id);
                comUtils.write_byte(' ');
                int msgLength = Integer.parseInt(userInput);
                comUtils.write_byte(msgLength);
                System.out.println("Cuales? (indica los indices separados por un espacio)");
                userInput = scanner.nextLine();
                userInput += " ";
                for (int i = 0; i < msgLength; i++) {
                    comUtils.write_byte(' ');
                    comUtils.write_byte(Integer.parseInt(userInput.substring(2 * i, 2 * i + 1)));
                }
            }else{
                System.out.println("Error en el protocolo");
                System.exit(0);
            }

            response = comUtils.read_string();
        }

        if (!response.equals("PNTS")){
            System.out.println("Error en el protocolo PNTS");
            System.exit(0);
        }
        comUtils.read_byte();
        System.out.println("Puntuacio: " + comUtils.read_byte());
    }
}
