package ca.mcgill.ecse321.petshelter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.petshelter.dto.PetDTO;
import ca.mcgill.ecse321.petshelter.model.User;
import ca.mcgill.ecse321.petshelter.repository.UserRepository;
import ca.mcgill.ecse321.petshelter.service.PetService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/pet")
public class PetController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PetService petService;
	
	
	@GetMapping("/{id}")
	public ResponseEntity<?> getPet(@RequestHeader String token, @PathVariable long id) {
		User requester = userRepository.findUserByApiToken(token);
		if (requester != null) {
			return new ResponseEntity<>(petService.getPet(id), HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	@GetMapping("/all")
	public ResponseEntity<?> getAllPets(@RequestHeader String token) {
		User requester = userRepository.findUserByApiToken(token);
		if (requester != null) {
			return new ResponseEntity<>(petService.getAllPets(), HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	@GetMapping("/all/{user}")
	public ResponseEntity<?> getAllPetsFromUser(@RequestHeader String token, @PathVariable String user) {
		User requester = userRepository.findUserByApiToken(token);
		if (requester != null) {
			return new ResponseEntity<>(petService.getPetsByUser(user), HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	@GetMapping("/all/{advertisement}")
	public ResponseEntity<?> getAllPetsFromAdvertisement(@RequestHeader String token,
			@PathVariable long advertisement) {
		User requester = userRepository.findUserByApiToken(token);
		if (requester != null) {
			return new ResponseEntity<>(petService.getPetsByAdvertisement(advertisement), HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deletePet(@RequestHeader String token, @PathVariable long id) {
		User requester = userRepository.findUserByApiToken(token);
		if (requester != null) {
			petService.deletePet(id, requester.getUserName());
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Changes the ownership of a pet
	 * 
	 * @param token
	 * @param id
	 * @param pet
	 * @return
	 */
	@PutMapping("/{id}")
	public ResponseEntity<?> updatePet(@RequestHeader String token, @PathVariable long id, @RequestBody PetDTO pet) {
		User requester = userRepository.findUserByApiToken(token);
		if (requester != null) {
			PetDTO pet2 = petService.editPet(pet);
			return new ResponseEntity<>(pet2, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Creates a pet
	 * 
	 * @param token
	 * @param pet
	 * @return
	 */
	@PostMapping()
	public ResponseEntity<?> createPet(@RequestHeader String token, @RequestBody PetDTO pet) {
		User requester = userRepository.findUserByApiToken(token);
		if (requester != null && requester.getUserName().equals(pet.getUserName())) {
			PetDTO createdPet = petService.createPet(pet);
			return new ResponseEntity<>(createdPet, HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}
