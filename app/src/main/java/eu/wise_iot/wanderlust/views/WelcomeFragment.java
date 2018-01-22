package eu.wise_iot.wanderlust.views;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import eu.wise_iot.wanderlust.R;
import eu.wise_iot.wanderlust.constants.Constants;
import eu.wise_iot.wanderlust.controllers.WelcomeController;

/**
 * WelcomeFragment:
 *
 * @author Fabian Schwander
 * @license MIT
 */
public class WelcomeFragment extends Fragment {

    private WelcomeController welcomeController;

    public WelcomeFragment() {
        welcomeController = new WelcomeController();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView backgroundImage = (ImageView) view.findViewById(R.id.background_image);
        Picasso.with(getActivity()).load(R.drawable.image_bg_welcome_screen).fit().centerCrop().into(backgroundImage);

        //show welcome message generated by a controller
        Toast.makeText(getActivity(), R.string.msg_welcome, Toast.LENGTH_SHORT).show();

        Button goToManualButton = (Button) view.findViewById(R.id.go_to_manual_button);
        goToManualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManualFragment fragment = new ManualFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, fragment, Constants.MANUAL_FRAGMENT)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }
}
