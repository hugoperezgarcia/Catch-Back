package com.Backend.AtrapaUnMillon.services;

import com.Backend.AtrapaUnMillon.exceptions.AdminBadRequestException;
import com.Backend.AtrapaUnMillon.exceptions.PreguntaBadRequestException;
import com.Backend.AtrapaUnMillon.models.Admin;
import com.Backend.AtrapaUnMillon.models.Pregunta;
import com.Backend.AtrapaUnMillon.repositories.AdminRepository;
import com.Backend.AtrapaUnMillon.repositories.PreguntaRepository;
import com.Backend.AtrapaUnMillon.utils.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PreguntaService {

    @Autowired
    private PreguntaRepository preguntaRepository;

    @Autowired
    private AdminRepository adminRepository;

    public List<Pregunta> getAllPreguntas(){
        return preguntaRepository.findAll();
    }

    public Pregunta getPreguntaById(Long id){
        Optional<Pregunta> pregunta = preguntaRepository.findById(id);
        if(pregunta.isPresent()){
            return pregunta.get();
        }else{
            throw new PreguntaBadRequestException("No existe pregunta con ese id");
        }
    }

    public List<Pregunta> getPreguntaByIdAdmin(Long idAdmin){
        List<Pregunta> preguntas = preguntaRepository.findByAdminId(idAdmin);
        return preguntas;
    }

    public Pregunta createPregunta(String pregunta, String respuestaCorrecta, String respuesta1,
                               String respuesta2, String respuesta3, String nivel,
                               String dificultad, String asignatura, int tiempo,
                               MultipartFile imagen, Long idAdmin) throws IOException {
        Optional<Admin> optionalAdmin = adminRepository.findById(idAdmin);
        if(optionalAdmin.isPresent()){
            Admin admin = optionalAdmin.get();
            byte[] bytesImg;
            if(imagen != null){
                bytesImg = ImageUtils.compressImage(imagen.getBytes());
            }else {
                bytesImg = null;
            }
            Pregunta new_pregunta = new Pregunta(pregunta, respuestaCorrecta, respuesta1, respuesta2,
                    respuesta3, nivel, dificultad, asignatura, tiempo, bytesImg, admin);
            preguntaRepository.save(new_pregunta);
            return new_pregunta;
        }else{
            throw new AdminBadRequestException("No existe admin");
        }
    }

    public List<Pregunta> procesarAsignarPreguntas(MultipartFile file, Long idAdmin) throws IOException {
        String nombreArchivo = file.getOriginalFilename();
        String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
        if(!extension.equals(".csv")){
            throw new IOException("Extensión de archivo no permitida");
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            List<Pregunta> nuevas_preguntas = new ArrayList<>();
            Optional<Admin> adminOptional = adminRepository.findById(idAdmin);
            int lineCount = 0;
            while ((line = br.readLine()) != null) {
                if (lineCount > 0) { // Saltar la primera línea
                    String[] data = line.split("[,;]");
                    for(int i = 0; i < data.length; i++){
                        System.out.println(data[i]);
                    }
                    System.out.println(data.length);
                    if(data.length < 9){
                        throw new IOException("Datos incompletos en la línea " + lineCount);
                    }
                    Pregunta new_pregunta = new Pregunta();
                    if (adminOptional.isPresent()) {
                        Admin admin = adminOptional.get();
                        new_pregunta.setAdmin(admin);

                        new_pregunta.setAsignatura(data[0]);
                        new_pregunta.setDificultad(data[1]);
                        new_pregunta.setNivel(data[2]);
                        new_pregunta.setPregunta(data[3]);
                        new_pregunta.setRespuestaCorrecta(data[4]);
                        new_pregunta.setRespuesta1(data[5]);
                        new_pregunta.setRespuesta2(data[6]);
                        new_pregunta.setRespuesta3(data[7]);
                        new_pregunta.setTiempo(Integer.parseInt(data[8]));

                        preguntaRepository.save(new_pregunta);
                        nuevas_preguntas.add(new_pregunta);
                    } else {
                        throw new AdminBadRequestException("No se encontró un administrador con el ID especificado");
                    }
                }
                lineCount++;
            }
            return nuevas_preguntas;
        } catch (IOException e) {
            throw new IOException("Error al procesar el archivo CSV: " + e.getMessage());
        }
    }

    public Pregunta editPregunta(Pregunta existingPregunta, String pregunta, String respuestaCorrecta,
                                 String respuesta1, String respuesta2, String respuesta3, String nivel,
                                 String dificultad, String asignatura, int tiempo, MultipartFile imagen, Long idAdmin) throws IOException {
        Optional<Admin> optionalAdmin = adminRepository.findById(idAdmin);
        if(optionalAdmin.isPresent()){
            Admin admin = optionalAdmin.get();
            byte[] bytesImg;
            if(imagen != null){
                bytesImg = ImageUtils.compressImage(imagen.getBytes());
            }else {
                bytesImg = null;
            }
            if(admin == existingPregunta.getAdmin()){
                existingPregunta.setPregunta(pregunta);
                existingPregunta.setRespuestaCorrecta(respuestaCorrecta);
                existingPregunta.setRespuesta1(respuesta1);
                existingPregunta.setRespuesta2(respuesta2);
                existingPregunta.setRespuesta3(respuesta3);
                existingPregunta.setNivel(nivel);
                existingPregunta.setDificultad(dificultad);
                existingPregunta.setAsignatura(asignatura);
                existingPregunta.setTiempo(tiempo);
                existingPregunta.setImagen(bytesImg);
                preguntaRepository.save(existingPregunta);
                return existingPregunta;
            }else{
                throw new PreguntaBadRequestException("No coincide el admin creador con el que edita");
            }
        }else{
            throw new AdminBadRequestException("No existe admin");
        }
    }

    public byte[] descargarFoto(Long id) {
        Pregunta existing_pregunta = preguntaRepository.findById(id).orElse(null);
        if (existing_pregunta != null){
            return ImageUtils.decompressImage(existing_pregunta.getImagen());
        }else{
            return null;
        }
    }
}
