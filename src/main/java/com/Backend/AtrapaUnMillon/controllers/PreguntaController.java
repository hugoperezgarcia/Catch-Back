package com.Backend.AtrapaUnMillon.controllers;


import com.Backend.AtrapaUnMillon.exceptions.AdminBadRequestException;
import com.Backend.AtrapaUnMillon.exceptions.PreguntaBadRequestException;
import com.Backend.AtrapaUnMillon.exceptions.ResponseWrapper;
import com.Backend.AtrapaUnMillon.models.Admin;
import com.Backend.AtrapaUnMillon.models.Partida;
import com.Backend.AtrapaUnMillon.models.Pregunta;
import com.Backend.AtrapaUnMillon.services.AdminService;
import com.Backend.AtrapaUnMillon.services.PreguntaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PreguntaController {
    @Autowired
    private PreguntaService preguntaService;

    @Operation(summary = "Obtiene todas las preguntas", tags = {"preguntas"})
    @ApiResponse(responseCode = "200", description = "Listado de pregunntas")
    @ApiResponse(responseCode = "404", description = "No hay preguntas")
    @GetMapping("/preguntas")
    public List<Pregunta> getAllPreguntas(){
        return preguntaService.getAllPreguntas();
    }

    @Operation(summary = "Obtiene una pregunta por id", tags = {"preguntas"})
    @ApiResponse(responseCode = "200", description = "Pregunta")
    @ApiResponse(responseCode = "404", description = "No hay preguntas")
    @Parameter(name = "id", required = true, description = "ID de la pregunta", example = "1")
    @GetMapping("/pregunta/{id}")
    public ResponseEntity<Pregunta> getPregunta(@PathVariable Long id){
        try{
            Pregunta pregunta = preguntaService.getPreguntaById(id);
            return new ResponseEntity<>(pregunta, HttpStatus.OK);
        }catch (PreguntaBadRequestException exception){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Muestra foto", description = "Obtiene foto de pregunta dado el id", tags = {"preguntas"})
    @Parameter(name = "id", description = "ID de la pregunta", required = true, example = "8")
    @ApiResponse(responseCode = "200", description = "Foto de la pregunta")
    @ApiResponse(responseCode = "404", description = "Pregunta no encontrada")
    @GetMapping(value = "/pregunta/{id}/foto", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> descargarFoto(@PathVariable Long id) {
        byte[] foto = preguntaService.descargarFoto(id);
        if ( foto != null ) {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(foto);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Obtiene una pregunta por id de admin", tags = {"preguntas"})
    @ApiResponse(responseCode = "200", description = "Pregunta")
    @ApiResponse(responseCode = "404", description = "No hay preguntas")
    @Parameter(name = "idAdmin", required = true, description = "ID del admin", example = "1")
    @GetMapping("/preguntaAdmin/{idAdmin}")
    public List<Pregunta> getPreguntaByAdmin(@PathVariable Long idAdmin){
        return preguntaService.getPreguntaByIdAdmin(idAdmin);
    }

    @Operation(summary = "Crea una pregunta", tags = {"preguntas"})
    @ApiResponse(responseCode = "201", description = "Pregunta creada")
    @ApiResponse(responseCode = "400", description = "Error al crear pregunta")
    @PostMapping(value = "/pregunta", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Pregunta> createPregunta(@RequestParam String pregunta,
                                                   @RequestParam String respuestaCorrecta,
                                                   @RequestParam String respuesta1,
                                                   @RequestParam String respuesta2,
                                                   @RequestParam String respuesta3,
                                                   @RequestParam String nivel,
                                                   @RequestParam String dificultad,
                                                   @RequestParam String asignatura,
                                                   @RequestParam int tiempo,
                                                   @RequestParam Long idAdmin,
                                                   @RequestPart (name="imagen", required=false)MultipartFile imagen) throws IOException{
        try{
            Pregunta nueva_pregunta = preguntaService.createPregunta(pregunta, respuestaCorrecta, respuesta1,
                    respuesta2, respuesta3, nivel,
                    dificultad, asignatura, tiempo, imagen, idAdmin);
            return new ResponseEntity<>(nueva_pregunta, HttpStatus.CREATED);
        }catch(AdminBadRequestException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Crea una pregunta", tags = {"preguntas"})
    @ApiResponse(responseCode = "201", description = "Pregunta creada")
    @ApiResponse(responseCode = "400", description = "Error al crear pregunta")
    @PostMapping(value = "/preguntacsv/{idAdmin}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ResponseWrapper<List<Pregunta>>> createPreguntaCsv(
            @PathVariable Long idAdmin,
            @RequestParam (name="archivo") MultipartFile file){
        ResponseWrapper<List<Pregunta>> response = new ResponseWrapper<>();

        try{
            List<Pregunta> nueva_pregunta = preguntaService.procesarAsignarPreguntas(file, idAdmin);
            response.setData(nueva_pregunta);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }catch(IOException e) {
            response.setErrorMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (AdminBadRequestException e){
            response.setErrorMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Edita una pregunta", tags = {"preguntas"})
    @ApiResponse(responseCode = "200", description = "Pregunta editada")
    @ApiResponse(responseCode = "400", description = "Error al editar pregunta")
    @PutMapping(value = "/pregunta/{preguntaId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Pregunta> updatePregunta(@PathVariable Long preguntaId,
                                                    @RequestParam String pregunta,
                                                   @RequestParam String respuestaCorrecta,
                                                   @RequestParam String respuesta1,
                                                   @RequestParam String respuesta2,
                                                   @RequestParam String respuesta3,
                                                   @RequestParam String nivel,
                                                   @RequestParam String dificultad,
                                                   @RequestParam String asignatura,
                                                   @RequestParam int tiempo,
                                                   @RequestParam Long idAdmin,
                                                   @RequestPart (name="imagen", required=false)MultipartFile imagen) throws IOException{
        try{
            Pregunta existingPregunta = preguntaService.getPreguntaById(preguntaId);
            try{
                Pregunta edited_pregunta =  preguntaService.editPregunta(existingPregunta, pregunta, respuestaCorrecta, respuesta1,
                        respuesta2, respuesta3, nivel,
                        dificultad, asignatura, tiempo, imagen, idAdmin);
                return new ResponseEntity<>(edited_pregunta, HttpStatus.CREATED);
            }catch(IOException e){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }catch(RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
