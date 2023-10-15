package com.todolist.todolist.task;

import com.todolist.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request){
        var idUser = request.getAttribute("idUser");
        taskModel.setIdUser((UUID) idUser);

        var currentDate = LocalDateTime.now();
        if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("data invalida");
        }

        if(taskModel.getStartAt().isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de inicio nao pode ser maior que a data de fim");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.taskRepository.save(taskModel));
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request){
        var idUser = request.getAttribute("idUser");
        return this.taskRepository.findAllByIdUser((UUID) idUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id){
        var taskSelected = this.taskRepository.findById(id).orElse(null);
        if(taskSelected == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tarefa não encontrada");
        }
        var idUser = request.getAttribute("idUser");
        if(!taskSelected.getIdUser().equals(idUser)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autorizado");
        }
        Utils.copyNonNullProperties(taskModel, taskSelected);

        return ResponseEntity.ok(this.taskRepository.save(taskSelected).toString());
    }
}
