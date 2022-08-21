package cinema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
public class SeatsController {
    private cinemaRoom cinemaRoom = new cinemaRoom(9 ,9);

    @GetMapping("/seats")
    public cinemaRoom getCinemaRoom() {
        return cinemaRoom;
    }

    @PostMapping("/purchase")
    public ResponseEntity postSeat(@RequestBody Purchase purchase) {
        // На минус надо тоже проверять оказывается
        if (purchase.getColumn() > 9 || purchase.getRow() > 9 || purchase.getColumn() < 1 || purchase.getRow() < 1) {
            return new ResponseEntity<>(new ApiError("The number of a row or a column is out of bounds!"), HttpStatus.BAD_REQUEST);
        }
        Seat seat = cinemaRoom.getAvailableSeats().get((purchase.getRow() - 1) * 9 + purchase.getColumn() - 1);
        UUID uuid;
        if (seat.isAvailable() == true) {
            uuid = this.cinemaRoom.addToTickets(seat);
            seat.setAvailable(false);
        } else {
            return new ResponseEntity<>(new ApiError("The ticket has been already purchased!"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new PurchasedTicket(uuid,seat), HttpStatus.OK);
    }

    @PostMapping("/return")
    public ResponseEntity returnTicket(@RequestBody Token token) {
        // Проверяем на наличие token в Hashmap tickets
        UUID uuidToken = token.getToken();
        if (this.cinemaRoom.checkToken(uuidToken)) {
            Seat seat = this.cinemaRoom.removeTicket(uuidToken);
            return new ResponseEntity<>(new ReturnedTicket(seat), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiError("Wrong token!"), HttpStatus.BAD_REQUEST);
    }


    @PostMapping("/stats")
    public ResponseEntity returnStatistics(@RequestParam(value = "password", required = false) String password) {
        if (password == null || !password.equals("super_secret")) {
            return new ResponseEntity<>(new ApiError("The password is wrong!"), HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(new Statistics(this.cinemaRoom), HttpStatus.OK);
    }

}

class Purchase {
    private int row;
    private int column;

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}

class cinemaRoom {
    private int totalRows;
    private int totalColumns;
    private List<Seat> seats = new ArrayList<>();

    @JsonIgnore
    private HashMap<UUID, Seat> tickets = new HashMap<>();
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

    public HashMap<UUID, Seat> getTickets() {
        return this.tickets;
    }

    public UUID addToTickets(Seat seat) {
        UUID uuid = UUID.randomUUID();
        tickets.put(uuid, seat);
        return uuid;
    }

    public boolean checkToken(UUID token) {
        return tickets.containsKey(token);
    }

    public Seat removeTicket(UUID token) {
        // возвращаем место в пул доступных
        Seat seat = tickets.get(token);
        seat.setAvailable(true);
        // удаляем билет из HashMap
        tickets.remove(token);
        return seat;
    }
}

class Seat {
    private int row;
    private int column;
    private int price;
    @JsonIgnore
    private boolean available;


    public Seat(int row, int column) {
        this.row = row;
        this.column = column;
        this.price = row <= 4 ? 10 : 8;
        this.available = true;
    }
    public int getRow() {
        return this.row;
    }
    public int getColumn() {
        return this.column;
    }
    public int getPrice() { return this.price; }
    public boolean isAvailable() { return this.available; }

    public void setAvailable(boolean available) {
        this.available = available;
    }


}

class ApiError {
    private String error;
    public ApiError(String error) {
        this.error = error;
    }
    public String getError() {
        return error;
    }
}

class PurchasedTicket {
    private UUID token;
    private Seat ticket;
    public PurchasedTicket(UUID uuid, Seat seat) {
        this.token = uuid;
        this.ticket = seat;
    }
    public UUID getToken() {
        return this.token;
    }
    public Seat getTicket() {
        return this.ticket;
    }
}

class ReturnedTicket {
    private Seat returnedTicket;
    public ReturnedTicket (Seat seat) {
        this.returnedTicket = seat;
    }

    public Seat getReturnedTicket() {
        return returnedTicket;
    }
}

class Token {
    private UUID token;
    public UUID getToken() {
        return this.token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }
}

class Statistics {
    private int currentIncome;
    private int numberOfAvailableSeats;
    private int numberOfPurchasedTickets;
    public Statistics(cinemaRoom cinemaRoom) {

        HashMap<UUID, Seat> tickets = cinemaRoom.getTickets();
        currentIncome = tickets.values().stream().mapToInt(Seat::getPrice).sum();
        numberOfPurchasedTickets = (int) tickets.keySet().stream().count();
        numberOfAvailableSeats = cinemaRoom.getTotalColumns() * cinemaRoom.getTotalColumns() - numberOfPurchasedTickets;
    }

    public int getCurrentIncome() {
        return this.currentIncome;
    }

    public int getNumberOfAvailableSeats() {
        return this.numberOfAvailableSeats;
    }

    public int getNumberOfPurchasedTickets() {
        return this.numberOfPurchasedTickets;
    }
}
