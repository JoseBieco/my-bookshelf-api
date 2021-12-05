package com.example.demo.user

import com.example.demo.configuration.UserDetailsServiceImpl
import com.example.demo.configuration.jwt.JwtService
import com.example.demo.user.dtos.LoginDto
import com.example.demo.user.dtos.RegisterUserDto
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    @Autowired
    val db: UserRepository,

    @Autowired
    val passwordEncoder: PasswordEncoder,

    @Autowired
    val userDetails: UserDetailsServiceImpl,

    @Autowired
    val jwtService: JwtService
) {

    /**
     * Register new user
     * @param user User
     * @return Created User
     * @throws HttpStatus.BAD_REQUEST Invalid user information
     * @throws HttpStatus.BAD_REQUEST This email is already registered
     */
    fun create(user: RegisterUserDto): User {

        if(!user.validate()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user information!")
        }

        /**
         * Validate email -> must be unique
         * throw error if it's not unique
         */
        if(this.db.searchEmail(user.email) > 0){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "This email is already registered")
        }

        return this.db.save(User(user, this.passwordEncoder))
    }

    /**
     * Login method
     * @param login LoginDto
     * @return Valid User
     * @throws HttpStatus.BAD_REQUEST Invalid email or password
     * @throws UsernameNotFoundException Email not registered yet
     * @throws HttpStatus.UNAUTHORIZED Unauthorized
     */
    fun login(login: LoginDto): User {
        /**
         * Validate email and password;
         * If not valid, throw 400;
         *
         * Check if email is registered and if the password match,
         * If the password not match, throw 401
         */
        if (!login.validate()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email or password!")
        }

        val user: User =  if (authenticate(login)) db.getNotOptionalByEmail(login.email)
        else throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized!")

        user.token = jwtService.generateToken(user)

        return user
    }

    /**
     * Delete entity by id
     * @param id Long
     * @throws HttpStatus.NOT_FOUND The entity with id does not exist
     */
    fun delete(id: Long) {
        if(!this.db.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "The entity with id $id does not exist")
        }
        return this.db.deleteById(id)
    }

    /**
     * Get entity by id
     * @param  id Long
     * @throws HttpStatus.NOT_FOUND The entity with id does not exist
     * @return User
     */
    fun getById(id: Long): User {
        if(!this.db.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "The entity with id $id does not exist")
        }
        return this.db.getOne(id)
    }

    /**
     * Get user from sent token
     * @throws HttpStatus.UNAUTHORIZED Unauthorized
     * @return User
     */
    fun getUserFromToken(token: String?): User {
        if(token == null) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized!")
        }
        val id = Jwts.parser().setSigningKey("secret").parseClaimsJws(token).body.issuer.toLong()
        return this.getById(id);
    }

    fun authenticate(loginDto: LoginDto): Boolean {
        val user: UserDetails? = loginDto.email?.let { userDetails.loadUserByUsername(it) }
        return passwordEncoder.matches(loginDto.password, user?.password)
    }
}