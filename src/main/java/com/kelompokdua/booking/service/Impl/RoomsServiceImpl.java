package com.kelompokdua.booking.service.Impl;

import com.kelompokdua.booking.constan.ERooms;
import com.kelompokdua.booking.entity.Rooms;

import com.kelompokdua.booking.model.request.RoomsRequest;
import com.kelompokdua.booking.model.request.RoomsSearchRequest;
import com.kelompokdua.booking.model.response.RoomsResponse;
import com.kelompokdua.booking.repository.RoomsRepository;
import com.kelompokdua.booking.service.RoomsService;

import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RoomsServiceImpl implements RoomsService {

    private final RoomsRepository roomsRepository;

    @Override
    public RoomsResponse createRoom(RoomsRequest roomsRequest) {
        List<String> facilitySUPERIORS = List.of("Big Tv Screen", "Projector", "Sofa", "Minibar" , "Bathroom", "Mega Desk", "bed", "Land Line Telephone", "Coffee Machine");
        List<String> facilityVIP = List.of("Big Tv Screen", "Sofa", "Minibar" , "Bathroom", "Big Desk", "Land Line Telephone");
        List<String> facilitySTANDARD = List.of("Tv Screen", "Sofa", " Desk", "Intercom Telephone");
        Rooms newRoom = Rooms.builder()
                .name(roomsRequest.getName())
                .roomType(roomsRequest.getRoomType())
                .capacity(roomsRequest.getCapacity())
                .status(ERooms.AVAILABLE)
                .price(roomsRequest.getPrice())
                .build();
        if(newRoom.getRoomType().equals("SUPERIORS")){
            newRoom.setFacilities(facilitySUPERIORS.toString());
        } else if (newRoom.getRoomType().equals("VIP")) {
            newRoom.setFacilities(facilityVIP.toString());
        } else if (newRoom.getRoomType().equals("STANDARD")) {
            newRoom.setFacilities(facilitySTANDARD.toString());
        }else{
            newRoom.setFacilities("Room Is Not On Type Register");
        }
        Rooms saveRoom = roomsRepository.saveAndFlush(newRoom);
        return RoomsResponse.builder()
                .id(saveRoom.getId())
                .name(saveRoom.getName())
                .roomType(saveRoom.getRoomType())
                .capacity(saveRoom.getCapacity())
                .facilities(Collections.singletonList(saveRoom.getFacilities()))
                .build();
    }

    @Override
    public Page<Rooms> getAllRooms(RoomsSearchRequest roomsSearchRequest) {
        if (roomsSearchRequest.getPage() <= 0 ) {
            roomsSearchRequest.setPage(1);
        }
         Specification<Rooms> roomSpecification = findRoom(
                roomsSearchRequest.getId(),
                roomsSearchRequest.getName(),
                roomsSearchRequest.getRoomType(),
                roomsSearchRequest.getCapacity(),
                roomsSearchRequest.getFacilities(),
                roomsSearchRequest.getStatus(),
                roomsSearchRequest.getMinPrice(),
                roomsSearchRequest.getMaxPrice());
        Pageable pageable = PageRequest.of(roomsSearchRequest.getPage()-1,roomsSearchRequest.getSize());
        return roomsRepository.findAll(roomSpecification, pageable);
    }
    public Specification<Rooms> findRoom(String id, String name, String roomType,
                                         Integer capacity, String facilities, ERooms status,
                                         Long minPrice, Long maxPrice) {

        return (root, query, criteriaBuilder) -> {
            Predicate idPredicate = criteriaBuilder.equal(root.get("id"), id);
            Predicate namePredicate = criteriaBuilder.equal(root.get("name"), name);
            Predicate roomTypePredicate = criteriaBuilder.equal(root.get("roomType"), roomType);
            Predicate capacityPredicate = criteriaBuilder.equal(root.get("capacity"), capacity);
            Predicate facilitiesPredicate = criteriaBuilder.equal(root.get("facilities"), facilities);
            Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), status);
            Predicate minPricePredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
            Predicate maxPricePredicate = criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
            return criteriaBuilder.or(idPredicate, namePredicate, roomTypePredicate, capacityPredicate,
                    facilitiesPredicate, statusPredicate, minPricePredicate, maxPricePredicate);
        };
    };

    @Override
    public Rooms getByRoomId(String id) {
        Optional<Rooms> optionalRooms = roomsRepository.findById(id);
        if (optionalRooms.isPresent()) {
            return optionalRooms.get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room ID Not Found");
    }

    @Override
    public Rooms updateRoomById(Rooms rooms) {
        this.getByRoomId(rooms.getId());
        return roomsRepository.save(rooms);
    }

    @Override
    public void deleteRoomById(String id) {
        this.getByRoomId(id);
        roomsRepository.deleteById(id);

    }

}