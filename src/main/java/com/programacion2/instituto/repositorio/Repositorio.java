package com.programacion2.instituto.repositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.springframework.stereotype.Repository;


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

        PersistenceManager pm = pmf.getPersistenceManager();

        // La transacción agrupa las operaciones en una única unidad de trabajo.
        Transaction tx = pm.currentTransaction();

        try {

            tx.begin();


            pm.makePersistent(objeto);

            tx.commit();

        } catch (RuntimeException e) {

            // Si algo falla deshacemos todo
            if (tx.isActive()) {
                tx.rollback();
            }


            throw e;

        } finally {

            // liberamos los recursos del PersistenceManager.
            pm.close();
        }
    }

    @Override
    public T consultar(Class<T> clase, int id) {

        PersistenceManager pm = pmf.getPersistenceManager();

       
        pm.getFetchPlan().setMaxFetchDepth(2);

        Transaction tx = pm.currentTransaction();

        try {
            tx.begin();


            T objeto = pm.getObjectById(clase, id);


            pm.retrieve(objeto);

  
            T separado = pm.detachCopy(objeto);

            tx.commit();

            return separado;

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

    
        pm.getFetchPlan().setMaxFetchDepth(2);

        Transaction tx = pm.currentTransaction();

        Query<T> query = null;

        try {
            tx.begin();

        
            query = pm.newQuery(clase);
            List<T> resultados = query.executeList();
            pm.retrieveAll(resultados, true);
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

            if (query != null) {
                query.closeAll();
            }

            pm.close();
        }
    }

    @Override
    public void eliminar(Class<T> clase, int id) {

        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();

        try {
            tx.begin();

         
            T objeto = pm.getObjectById(clase, id);
            pm.deletePersistent(objeto);

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

    @Override
    public List<T> buscar(Class<T> clase, String campo, String texto) {


        if (texto == null || texto.isBlank()) {
            return listar(clase);
        }


        if (campo == null || !campo.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Nombre de campo inválido: " + campo);
        }

        PersistenceManager pm = pmf.getPersistenceManager();

        // Profundidad máxima de las relaciones que se recuperan.
        pm.getFetchPlan().setMaxFetchDepth(2);

        Transaction tx = pm.currentTransaction();

        Query<T> query = null;

        try {
            tx.begin();

       
            query = pm.newQuery(clase);
            query.setFilter("this." + campo + ".toLowerCase().indexOf(:texto) >= 0");
            query.setParameters(texto.toLowerCase(Locale.ROOT));
            List<T> resultados = query.executeList();


            pm.retrieveAll(resultados, true);

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

            if (query != null) {
                query.closeAll();
            }

            pm.close();
        }
    }
}
