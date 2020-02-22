package ca.mcgill.ecse321.petshelter.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.petshelter.dto.CommentDTO;
import ca.mcgill.ecse321.petshelter.dto.ForumDTO;
import ca.mcgill.ecse321.petshelter.dto.UserDTO;
import ca.mcgill.ecse321.petshelter.model.Comment;
import ca.mcgill.ecse321.petshelter.model.Forum;
import ca.mcgill.ecse321.petshelter.model.User;
import ca.mcgill.ecse321.petshelter.model.UserType;
import ca.mcgill.ecse321.petshelter.repository.ForumRepository;
import ca.mcgill.ecse321.petshelter.repository.UserRepository;
import ca.mcgill.ecse321.petshelter.service.ForumService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/forum")
public class ForumController {

	@Autowired
	ForumRepository forumRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ForumService forumService;

	/**
	 * Gets the desired forum and its associated comments.
	 * 
	 * @param id Forum ID of the desired forum.
	 * @return The forum DTO.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<?> getForum(@PathVariable(required = true) Long id) {
		Forum forum = forumRepository.findForumById(id);
		if (forum == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} else
			return new ResponseEntity<>(forumToDto(forum), HttpStatus.OK);
	}

	/**
	 * Gets all existing forums thread.
	 *
	 * @return List of all existing forum threads.
	 */
	@GetMapping("/all")
	public ResponseEntity<?> getAllForums() {
		List<Forum> forums = new ArrayList<>();
		List<ForumDTO> forumsDto = new ArrayList<>();
		Iterable<Forum> usersIterable = forumRepository.findAll();
		usersIterable.forEach(forums::add);
		// Convert the forums into forumdtos
		for (Forum f : forums) {
			forumsDto.add(forumToDto(f));
		}
		return new ResponseEntity<>(forumsDto, HttpStatus.OK);
	}

	/**
	 * Create new forum thread.
	 * @param title The title of the forum to create.
	 * @param token The session token of the user.
	 * @return The created forum.
	 */
	@PostMapping("/")
	public ResponseEntity<?> createForum(@RequestBody String title, @RequestHeader String token) {
		User user = userRepository.findUserByApiToken(token);
		if (user != null && title != null && !title.trim().equals("")) {
			Forum forum = forumService.addForum(title, user);
			return new ResponseEntity<ForumDTO>(forumToDto(forum), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Lock or unlock a given forum. By design, only admin are allowed to lock and unlock forums.
	 * @param forumID The id of a given forum.
	 * @param token The session token of a user.
	 * @param isLocked The status to set the forum thread to.
	 * @return The modified forum.
	 */
	@PutMapping("/{forumId}")
	public ResponseEntity<?> setLockForum(@PathVariable long forumID, @RequestHeader String token,
										  @RequestBody Boolean isLocked) {
		User user = userRepository.findUserByApiToken(token);
		if (user != null && user.getUserType() == UserType.ADMIN) {
			Forum forum;
			if (isLocked) {
				forum = forumService.lockForum(forumID);
			} else {
				forum = forumService.unlockForum(forumID);
			}
			return new ResponseEntity<ForumDTO>(forumToDto(forum), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Delete a forum thread from the database. By design, only an admin may delete a forum thread.
	 * @param forumId Forum id of the forum to delete.
	 * @param token Session token of the user.
	 * @return The deleted forum.
	 */
	@DeleteMapping("/{forumId}")
	public ResponseEntity<?> deleteForum(@PathVariable long forumId, @RequestHeader String token) {
		User user = userRepository.findUserByApiToken(token);
		Forum forum = forumRepository.findForumById(forumId);
		if (user != null && forum != null && user.getUserType().equals(UserType.ADMIN)) {
			// delete all comments
			
			Set<Comment> comments = forum.getComments();
			for (Iterator<Comment> it = comments.iterator(); it.hasNext();) {
				it.next();
				it.remove();
			}
			forumRepository.delete(forum);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Convert a forum thread to a forum DTO.
	 * 
	 * @param forum The forum to convert.
	 * @return A forum DTO.
	 */
	static ForumDTO forumToDto(Forum forum) {
		ForumDTO forumDTO = new ForumDTO();
		Set<UserDTO> subscribers = new HashSet<UserDTO>();
		Set<CommentDTO> comments = new HashSet<CommentDTO>();
		forum.getSubscribers().forEach(u -> subscribers.add(UserController.userToDto(u)));
		forum.getComments().forEach(c -> comments.add(CommentController.commentToDto(c)));
		forumDTO.setId(forum.getId());
		forumDTO.setTitle(forum.getTitle());
		forumDTO.setComments(comments);
		forumDTO.setSubscribers(subscribers);
		return forumDTO;
	}
}
