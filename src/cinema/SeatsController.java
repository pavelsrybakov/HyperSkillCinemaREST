package cinema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SeatsController {
    private cinemaRoom cinemaRoom = new cinemaRoom(9 ,9);

    @GetMapping("/seats")
    public cinemaRoom getCinemaRoom() {
        return cinemaRoom;
    }

}

class cinemaRoom {
    private int totalRows;
    private int totalColumns;
    private List<Seat> seats = new ArrayList<>();
    public cinemaRoom(int rows, int columns) {
        this.totalRows = rows;
        this.totalColumns = columns;
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= columns; j++) {
                seats.add(new Seat(i , j));
            }
        }
    }
    public List<Seat> getAvailableSeats() {
        return this.seats;
    }
    public int getTotalRows() {
        return this.totalRows;
    }
    public int getTotalColumns() {
        return this.totalColumns;
    }
}

class Seat {
    private int row;
    private int column;
    private int price;
    private boolean available;


    public Seat(int row, int column) {
        this.row = row;
        this.column = column;
    }
    public int getRow() {
        return this.row;
    }
    public int getColumn() {
        return this.column;
    }

}
