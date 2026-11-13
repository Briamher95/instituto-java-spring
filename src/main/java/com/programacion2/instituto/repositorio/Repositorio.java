package com.programacion2.instituto.repositorio;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.springframework.stereotype.Repository;

// Un único bean genérico: sirve para cualquier clase persistente.
@Repository
public class Repositorio<T> implements IRepositorio<T> {

    private final PersistenceManagerFactory pmf;

    // Spring inyecta la fábrica de persistencia por el único constructor.
    public Repositorio(PersistenceManagerFactory pmf) {
        this.pmf = pmf;
    }

    @Override
    public void guardar(T objeto) {

        // Obtenemos un PersistenceManager: administra los objetos persistentes
        // y permite operar sobre la base de datos.
        PersistenceManager pm = pmf.getPersistenceManager();

        // La transacción agrupa las operaciones en una única unidad de trabajo.
        Transaction tx = pm.currentTransaction();

        try {
            // Iniciamos la transacción.
            tx.begin();

            // Hacemos persistente el objeto (INSERT al confirmar la transacción).
            pm.makePersistent(objeto);

            // Confirmamos: se guardan definitivamente los cambios.
            tx.commit();

        } catch (RuntimeException e) {

            // Si algo falló y la transacción sigue activa, deshacemos todo.
            if (tx.isActive()) {
                tx.rollback();
            }

            // Relanzamos la excepción para que quien llamó conozca el error.
            throw e;

        } finally {

            // Se ejecuta siempre: liberamos los recursos del PersistenceManager.
            pm.close();
        }
    }

    @Override
    public T consultar(Class<T> clase, int id) {

        PersistenceManager pm = pmf.getPersistenceManager();

        // Al confirmar, el objeto se devuelve detached (usable tras cerrar el pm).
        pm.setDetachAllOnCommit(true);

        // Profundidad máxima de las relaciones que se recuperan.
        pm.getFetchPlan().setMaxFetchDepth(2);

        Transaction tx = pm.currentTransaction();

        try {
            tx.begin();

            // Busca el objeto por su identificador.
            T objeto = pm.getObjectById(clase, id);

            // Fuerza la carga de los campos del FetchPlan.
            pm.retrieve(objeto);

            tx.commit();

            return objeto;

        } catch (RuntimeException e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;

        } finally {
            pm.close();
        }
    }

    @Override
    public List<T> listar(Class<T> clase) {

        PersistenceManager pm = pmf.getPersistenceManager();

        // Profundidad máxima de las relaciones que se recuperan.
        pm.getFetchPlan().setMaxFetchDepth(2);

        Transaction tx = pm.currentTransaction();

        Query<T> query = null;

        try {
            tx.begin();

            // Consulta sin filtro: trae todos los objetos de la clase.
            query = pm.newQuery(clase);
            List<T> resultados = query.executeList();

            // Fuerza la carga de los campos del FetchPlan en toda la lista.
            pm.retrieveAll(resultados, true);

            // Copias detached: se pueden usar después de cerrar el pm.
            List<T> resultadosSeparados = new ArrayList<>(
                    pm.detachCopyAll(resultados));

            tx.commit();

            return resultadosSeparados;

        } catch (RuntimeException e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;

        } finally {

            // Liberamos la consulta y luego el PersistenceManager.
            if (query != null) {
                query.closeAll();
            }

            pm.close();
        }
    }
}
