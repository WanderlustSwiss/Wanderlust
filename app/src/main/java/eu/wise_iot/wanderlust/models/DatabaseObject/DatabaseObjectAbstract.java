package eu.wise_iot.wanderlust.models.DatabaseObject;

import java.util.List;

import eu.wise_iot.wanderlust.controllers.FragmentHandler;
import eu.wise_iot.wanderlust.models.DatabaseModel.AbstractModel;

/**
 * DatabaseObjectAbstract
 *
 * @author Rilind Gashi
 * @license MIT
 */

public class DatabaseObjectAbstract implements DatabaseObject {

    public void create(final AbstractModel abstractModel, final FragmentHandler handler) {
        throw new UnsupportedOperationException();
    }

    public void retrieve(final AbstractModel abstractModel, final FragmentHandler handler) {
        throw new UnsupportedOperationException();
    }

    public void update(final AbstractModel abstractModel, final FragmentHandler handler) {
        throw new UnsupportedOperationException();
    }

    public List<? extends AbstractModel> find() {
        throw new UnsupportedOperationException();
    }

    public AbstractModel findOne(String searchedColumn, String searchPattern) {
        throw new UnsupportedOperationException();
    }

    public List<? extends AbstractModel> find(String searchedColumn, String searchPattern) {
        throw new UnsupportedOperationException();

    }

    public void delete(String searchedColumn, String searchPattern) {
        throw new UnsupportedOperationException();
    }

    public long count() {
        throw new UnsupportedOperationException();
    }

    public long count(String searchedColumn, String searchPattern) {
        throw new UnsupportedOperationException();
    }


}