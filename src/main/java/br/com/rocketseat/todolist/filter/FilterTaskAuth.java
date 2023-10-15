package br.com.rocketseat.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.rocketseat.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired IUserRepository userRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

                var servletPath = request.getServletPath();

                if (servletPath.startsWith("/tasks/")) {
                    
                    var authorization = request.getHeader("Authorization").substring("Basic".length()).trim();
                    byte[] authDecoded = Base64.getDecoder().decode(authorization);
                    String[] credentials = new String(authDecoded).split(":");
    
                    String username = credentials[0];
                    String password = credentials[1];
    
                    var user = this.userRepository.findByUsername(username);
    
                    if (user == null) {
                        response.sendError(401, "Usuário não existe");
                    } else {
    
                        var passwordVerified = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
    
                        if (passwordVerified.verified) {

                            request.setAttribute("idUser", user.getId());
                            filterChain.doFilter(request, response);            
                            
                        } else {
                            response.sendError(401,"Senha incorreta");
                        }
                    }

                } else {
                    
                     filterChain.doFilter(request, response);            

                }
    }

}
