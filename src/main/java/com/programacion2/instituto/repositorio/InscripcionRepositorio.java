package com.programacion2.instituto.repositorio;

import java.time.LocalDate;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

import org.springframework.stereotype.Repository;

import com.programacion2.instituto.dominio.Alumno;
import com.programacion2.instituto.dominio.Curso;
import com.programacion2.instituto.dominio.Inscripcion;

@Repository
public class InscripcionRepositorio implements IInscripcionRepositorio {

    private final PersistenceManagerFactory pmf;


    public InscripcionRepositorio(PersistenceManagerFactory pmf) {
        this.pmf = pmf;
    }


    @Override
    public void crear(int idAlumno, int idCurso, LocalDate fecha) {

        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try {
            tx.begin();

            // Alumno y curso se buscan dentro de esta transacción 
            Alumno alumno = pm.getObjectById(Alumno.class, idAlumno);
            Curso curso = pm.getObjectById(Curso.class, idCurso);

            pm.makePersistent(new Inscripcion(curso, alumno, fecha));

            tx.commit();

        } catch (RuntimeException e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;

        } finally {
            pm.close();
        }
    }
}
