package com.programacion2.instituto.repositorio;

import java.time.LocalDate;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

import org.springframework.stereotype.Repository;

import com.programacion2.instituto.dominio.Alumno;
import com.programacion2.instituto.dominio.Curso;
import com.programacion2.instituto.dominio.Inscripcion;

// Crea inscripciones por ids. 
@Repository
public class InscripcionRepositorio implements IInscripcionRepositorio {

    private final PersistenceManagerFactory pmf;

    // Spring inyecta la fábrica de persistencia por el único constructor.
    public InscripcionRepositorio(PersistenceManagerFactory pmf) {
        this.pmf = pmf;
    }

    // Sin reglas de negocio: las revisa InscripcionServicio antes de llamar acá.
    // Es el punto único donde, si algún día hubiera varios usuarios a la vez, se
    // agregarían el candado sobre el curso y la revisión de cupo y duplicado
    // dentro de esta misma transacción.
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
