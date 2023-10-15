package br.com.rocketseat.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.rocketseat.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")

public class TaskController {
    
    @Autowired
    private ITaskRepository taskRepository;

    @GetMapping("/listTasks")

    public List<TaskModel> listTasks(HttpServletRequest request) {

        var tasks = this.taskRepository.findByidUser((UUID) request.getAttribute("idUser"));
        return tasks;
    }

    @PostMapping("/createTask")

    public ResponseEntity createTask(@RequestBody TaskModel taskModel, HttpServletRequest request) { 
        
        taskModel.setIdUser( (UUID) request.getAttribute("idUser"));

        if (LocalDateTime.now().isAfter(taskModel.getStartAt()))  {
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("A data de início é anterior a data atual");
        }

        if (taskModel.getFinish().isBefore(taskModel.getStartAt())) {
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("A data de término deve ser maior que a data de início da tarefa");
        }

        
        var task = this.taskRepository.save(taskModel);

        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @PutMapping("/updateTask/{id}")

    public ResponseEntity updateTask(@RequestBody TaskModel taskModel
    ,HttpServletRequest request
    ,@PathVariable UUID id
    ) {
        var task = this.taskRepository.findById(id).orElse(null);

        var idUser = request.getAttribute("idUser");

        if (task == null) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Tarefa não encontrada");
        }
        
        if(!task.getIdUser().equals(idUser)) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Usuário sem permissão para alterar a tarefa");
        }
                        
        Utils.copyNonNullProperties(taskModel, task);
        
        var taskUpdated = this.taskRepository.save(task);

        return ResponseEntity.status(HttpStatus.OK).body(taskUpdated);
    }

}


