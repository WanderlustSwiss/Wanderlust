package eu.wise_iot.wanderlust.models.DatabaseObject;

import java.util.List;

import eu.wise_iot.wanderlust.controllers.ControllerEvent;
import eu.wise_iot.wanderlust.controllers.DatabaseController;
import eu.wise_iot.wanderlust.controllers.EventType;
import eu.wise_iot.wanderlust.controllers.FragmentHandler;
import eu.wise_iot.wanderlust.models.DatabaseModel.AbstractModel;
import eu.wise_iot.wanderlust.models.DatabaseModel.SavedTour;
import eu.wise_iot.wanderlust.services.SavedTourService;
import eu.wise_iot.wanderlust.services.ServiceGenerator;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.Property;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * CommunityTourDao:
 *
 * this model represents a saved tour out of the existing tours
 * it is important to distinguish CommunityTour with UserTour
 *
 *
 * @author Rilind Gashi, Alexander Weinbeck, Simon Kaspar
 * @license MIT
 */


public class CommunityTourDao extends DatabaseObjectAbstract {
    private static class Holder {
        private static final CommunityTourDao INSTANCE = new CommunityTourDao();
    }

    private static final BoxStore BOXSTORE = DatabaseController.getBoxStore();

    public static CommunityTourDao getInstance(){
        return BOXSTORE != null ? Holder.INSTANCE : null;
    }

    private final Box<SavedTour> communityTourBox;
    private final SavedTourService service;

    /**
     * Constructor.
     */

    private CommunityTourDao() {
        communityTourBox = BOXSTORE.boxFor(SavedTour.class);
        service = ServiceGenerator.createService(SavedTourService.class);
    }

    public long count() {
        return communityTourBox.count();
    }

    public long count(Property searchedColumn, String searchPattern) {
        return find(searchedColumn, searchPattern).size();
    }

    public long count(Property searchedColumn, long searchPattern) {
        return find(searchedColumn, searchPattern).size();
    }

    /**
     * Update an existing communityTour in the database.
     *
     * @param communityTour (required).
     */
    public SavedTour update(SavedTour communityTour) {
        communityTourBox.put(communityTour);
        return communityTour;
    }

    /**
     * insert a communityTour local and remote
     *
     * @param communityTour
     * @param handler
     */
    public void create(final AbstractModel communityTour, final FragmentHandler handler) {
        communityTourBox.put((SavedTour) communityTour);
    }

    /**
     * insert community tour only local
     *
     * @param communityTour
     */
    public void create(final SavedTour communityTour){

        communityTourBox.put(communityTour);
    }

    /**
     * get communityTour with specific id
     *
     * @param id
     */
    public SavedTour retrieve(int id) {
        return communityTourBox.get(id);
    }

    /**
     * get usertour out of the remote database by entity
     *
     * @param id
     * @param handler
     */

    public void retrieve(final long id, final FragmentHandler handler) {
        final long[] newUserTourID = new long[1];
        Call<SavedTour> call = service.retrieveTour(id);
        call.enqueue(new Callback<SavedTour>() {
            @Override
            public void onResponse(Call<SavedTour> call, Response<SavedTour> response) {
                if (response.isSuccessful()) {
                    SavedTour backendTour = response.body();
                    //routeBox.put(backendTour); wieso in die lokale db einfügen ??
                    newUserTourID[0] = backendTour.getInternal_id();
                    handler.onResponse(new ControllerEvent(EventType.getTypeByCode(response.code()), backendTour));
                } else {
                    handler.onResponse(new ControllerEvent(EventType.getTypeByCode(response.code())));
                }
            }

            @Override
            public void onFailure(Call<SavedTour> call, Throwable t) {
                handler.onResponse(new ControllerEvent(EventType.NETWORK_ERROR));
            }
        });
    }

    public SavedTour findOne(Property searchedColumn, String searchPattern) {
        return communityTourBox.query().equal(searchedColumn, searchPattern).build().findFirst();
    }

    public SavedTour findOne(Property searchedColumn, long searchPattern) {
        return communityTourBox.query().equal(searchedColumn, searchPattern).build().findFirst();
    }

    public List<SavedTour> find(Property searchedColumn, String searchPattern) {
        return communityTourBox.query().equal(searchedColumn, searchPattern).build().find();
    }

    public List<SavedTour> find(Property searchedColumn, long searchPattern) {
        return communityTourBox.query().equal(searchedColumn, searchPattern).build().find();
    }

    public List<SavedTour> find(Property searchedColumn, boolean searchPattern) {
        return communityTourBox.query().equal(searchedColumn, searchPattern).build().find();
    }

    public List<SavedTour> find() {
        return (communityTourBox != null) ? communityTourBox.getAll() : null;
    }

    public void deleteByPattern(Property searchedColumn, String searchPattern) {
        communityTourBox.remove(findOne(searchedColumn, searchPattern));
    }

    public void delete(SavedTour communityTour){
        List<SavedTour> list = communityTourBox.getAll();
        for(SavedTour t : list){
            if(t.getTour_id() == communityTour.getTour_id()){
                communityTourBox.remove(t);
            }
        }
    }

}
