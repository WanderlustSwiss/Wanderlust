package eu.wise_iot.wanderlust.controllers;

import eu.wise_iot.wanderlust.models.DatabaseModel.Favorite;
import eu.wise_iot.wanderlust.models.DatabaseModel.Favorite_;
import eu.wise_iot.wanderlust.models.DatabaseModel.UserTour;
import eu.wise_iot.wanderlust.models.DatabaseObject.FavoriteDao;
import eu.wise_iot.wanderlust.models.DatabaseObject.UserTourDao;

/**
 * ToursController:
 * handles the toursfragment and its in and output
 *
 * @author Alexander Weinbeck
 * @license MIT
 */
public class TourOverviewController {
    /**
     * get all required data for the view
     * @param handler
     */
    public static void getAllTours(FragmentHandler handler) {
        UserTourDao userTourDao = new UserTourDao();
        userTourDao.retrieveAll(handler);
    }
    /**
     * get all Favorites for the view
     * @param handler
     */
//    public static void getAllFavorites(FragmentHandler handler) {
//        UserTourDao userTourDao = new UserTourDao();
//        userTourDao.retrieveAll(handler);
//    }
    /**
     * get all tours out of db
     *
     * @return List of tours
     */
    public UserTour getDataView(int tourID) {

        UserTourDao userTourDao = new UserTourDao();
        return userTourDao.find().get(tourID);
    }
    /**
     * get thumbnail of each tour
     *
     */
    public static void downloadThumbnail(long tourID, int image_id, FragmentHandler handler) {
        DatabaseController.userTourDao.downloadImage(tourID, image_id, handler);
    }
    /**
     * get all Favorites
     *
     */
    public static void downloadFavorites(FragmentHandler handler) {
        FavoriteDao favoriteDao = new FavoriteDao();
        favoriteDao.retrievAllFavorites(handler);
    }
    /**
     * get all Favorites
     *
     */
    public static void setFavorite(UserTour tour, FragmentHandler handler) {
        FavoriteDao favoriteDao = new FavoriteDao();
        favoriteDao.create(tour,handler);
    }
    /**
     * get all Favorites
     *
     */
    public static void deleteFavorite(long favorite_id, FragmentHandler handler) {
        FavoriteDao favoriteDao = new FavoriteDao();
        favoriteDao.delete(favorite_id,handler);
    }

    public long getTourFavoriteId(long id){
        FavoriteDao favoriteDao = new FavoriteDao();
        try {
            Favorite fav = favoriteDao.findOne(Favorite_.tour, id);
            if(fav != null) return fav.getFav_id();
        } catch (Exception e){}
        return -1;
    }

}