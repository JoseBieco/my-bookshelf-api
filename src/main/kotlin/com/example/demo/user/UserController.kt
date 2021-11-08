package com.example.demo.user

import com.example.demo.user.dtos.LoginDto
import com.example.demo.user.dtos.RegisterUserDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("auth")
class UserController(
    @Autowired
    val service: UserService
) {
    /**
     *  Routes:
     *      Login -> /auth/login, method: POST
     *      Register -> /auth/register, method: POST
     *  Basic:
     *      Delete -> /auth, method: DELETE
     *      Update -> /auth/{userId}, method: PUT { Name, Password, Email?; }
     * */

    @PostMapping
    @RequestMapping("/register")
    fun register(@RequestBody user: RegisterUserDto): User {
        return this.service.create(user)
    }

    @PostMapping
    @RequestMapping("/login")
    fun login(@RequestBody login: LoginDto): User {
        return this.service.login(login)
    }

    @DeleteMapping
    @RequestMapping("/{userId}")
    fun delete(@PathVariable userId: Long) {
        return this.service.delete(userId)
    }
}