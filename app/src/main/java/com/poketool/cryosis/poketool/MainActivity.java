package com.poketool.cryosis.poketool;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int[] clickedIcons = new int[] {-1, -1};
    private boolean defending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void btnOffensiveClick(View v) {
        Button btnOffensive = (Button) v;
        if (!defending) {
            btnOffensive.setText("DEFENDING");
            defending = !defending;
        }
        else {
            btnOffensive.setText("ATTACKING");
            defending = !defending;
            if (clickedIcons[0] != -1 && clickedIcons[1] != -1) {
                iconPressed(findViewById(clickedIcons[0]));
            }
        }
        updateChart();
    }

    public void iconButtonHandler(View v) {
        iconPressed(v);
        updateChart();
    }

    /** public void iconPressed(View v) {
     * Adds or Removes a glow around an icon button
     * @param v The icon to update.
     */
    public void iconPressed(View v) {
        updatePressed(v.getId());

        if (v.getId() == clickedIcons[0] || v.getId() == clickedIcons[1])
            v.setBackground(getDrawable(R.drawable.icn_clicked_background));
        else
            v.setBackgroundResource(0);
    }

    /** private void updateChart() {
     *  Updates the effectiveness charts based on the types selected (as indicated by clickedIcons array)
     */
    private void updateChart() {
        removeIcons();

        // If at least one option is selected, update the types
        if (clickedIcons[0] != -1) {
            List<List<String>> typeChart = null;
            try {
                // Get a single type chart if only one button is selected or they are attacking.
                if (clickedIcons[1] == -1 || !defending) {
                    String type1 = getResources().getResourceEntryName(clickedIcons[0]).split("_|\\.")[1];
                    typeChart = (defending) ? Type.getDefender(type1) : Type.getAttacker(type1);
                }
                else {
                    String type1 = getResources().getResourceEntryName(clickedIcons[0]).split("_|\\.")[1];
                    String type2 = getResources().getResourceEntryName(clickedIcons[1]).split("_|\\.")[1];
                    typeChart = Type.getDefender(type1, type2);
                }

                displayEffectiveness(typeChart);

            } catch (Exception e) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                builder.setTitle("Error While Updating Chart.")
                        .setMessage(e.toString() + "\n" + e.getMessage())
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {}
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

            copySearchStringToClipboard(typeChart);
        }
    }

    /** private void copySearchStringToClipboard(List<List<String>> typeChart) {
     * Copies the super effective attacking types and super resistance defensive types
     * to the clipboard for searching in Pokemon Go.
     *
     * @param typeChart The attacking or defending effectiveness list that was fetched
     *                  prior to this method call via Type.getAttackers or Type.getDefenders.
     */
    private void copySearchStringToClipboard(List<List<String>> typeChart) {

        if (clickedIcons[0] != -1) {
            StringBuilder types = new StringBuilder();
            String minimumCP = "CP2000-10000";

            if (defending) {    // Loop through the first two in the list
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < typeChart.get(i).size(); j++) {
                        String s = typeChart.get(i).get(j);
                        types.append("@").append(s);
                        if (i < 1 || j < typeChart.get(i).size() - 1)
                            types.append(", ");
                    }
                }
            }
            else {              // Loop through the last three
                for (int i = typeChart.size() - 1; i > 2; i--) {
                    for (int j = 0; j < typeChart.get(i).size(); j++) {
                        String s = typeChart.get(i).get(j);
                        types.append(s);
                        if (i > 3 || j < typeChart.get(i).size() - 1)
                            types.append(", ");
                    }
                }
            }

            String searchTerm = types + "&" + minimumCP;
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("PoGo Search Term", searchTerm);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
        }
    }

    /** private void displayEffectiveness(List<List<String>> typeChart) {
     *  Iterates over the type chart to display the icon of each effectiveness.
     * @param typeChart A List of 6 ArrayLists, sorted by type effectiveness.
     */
    private void displayEffectiveness(List<List<String>> typeChart) {
        for (int i = 0; i < typeChart.size(); i++) {
            LinearLayout view = null;
            switch (i) {
                case 0:
                    view = findViewById(R.id.linear_extremely_effective);
                    break;
                case 1:
                    view = findViewById(R.id.linear_super_effective);
                    break;
                case 2:
                    view = findViewById(R.id.linear_normal_effectiveness);
                    break;
                case 3:
                    view = findViewById(R.id.linear_not_very_effective);
                    break;
                case 4:
                    view = findViewById(R.id.linear_extremely_ineffective);
                    break;
                case 5:
                    view = findViewById(R.id.linear_no_effect);
                    break;
            }

            // TODO: Handle error if view is not found.
            if (view != null) {
                Slide slide = new Slide();
                slide.setSlideEdge(Gravity.RIGHT);
                TransitionManager.beginDelayedTransition(view, slide);

                for (String s : typeChart.get(i)) {
                    ImageView img = new ImageView(this);
                    Resources res = getResources();
                    String mDrawableName = "icon_" + s;
                    int resID = res.getIdentifier(mDrawableName, "drawable", getPackageName());
                    Drawable drawable = ContextCompat.getDrawable(this, resID);
                    img.setImageDrawable(drawable);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.weight = 1;
                    img.setLayoutParams(params);

                    img.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    img.setPadding(10, 0, 10, 0);
                    view.addView(img);
                }
            }
        }
    }

    private void removeIcons() {
        Fade fade = new Fade();
        ViewGroup results = findViewById(R.id.tbl_results);
        TransitionManager.beginDelayedTransition(results, fade);

        LinearLayout layout = findViewById(R.id.linear_extremely_effective);
        layout.removeAllViews();
        layout = findViewById(R.id.linear_super_effective);
        layout.removeAllViews();
        layout = findViewById(R.id.linear_normal_effectiveness);
        layout.removeAllViews();
        layout = findViewById(R.id.linear_not_very_effective);
        layout.removeAllViews();
        layout = findViewById(R.id.linear_extremely_ineffective);
        layout.removeAllViews();
        layout = findViewById(R.id.linear_no_effect);
        layout.removeAllViews();
    }

    /** private void updatePressed(int id) {}
     * Manages an array(length 2) that operates like a queue, but an element can be
     * removed as well.
     * If the player has selected 'attacking' - then we do not need the second element.
     * @param id The id to add/remove from the 'clickedIcons' array.
     */
    private void updatePressed(int id) {
        // If the id is not in the list i.e. a new button is pressed.
        if (clickedIcons[0] != id && clickedIcons[1] != id) {
            if (clickedIcons[0] == -1)
                clickedIcons[0] = id;
            else if (defending && clickedIcons[1] == -1)
                clickedIcons[1] = id;
            else {
                ImageButton btn = findViewById(clickedIcons[0]);
                btn.setBackgroundResource(0);
                if (defending) {
                    clickedIcons[0] = clickedIcons[1];
                    clickedIcons[1] = id;
                }
                else {
                    clickedIcons[0] = id;
                    clickedIcons[1] = -1;
                }
            }
        }
        else if (clickedIcons[0] == id) {
            ImageButton btn = findViewById(clickedIcons[0]);
            btn.setBackgroundResource(0);
            clickedIcons[0] = clickedIcons[1];
            clickedIcons[1] = -1;
        }
        else {
            ImageButton btn = findViewById(clickedIcons[1]);
            btn.setBackgroundResource(0);
            clickedIcons[1] = -1;
        }
    }


    private static class Type {

        private static final String[] typeTitles = new String[] {"normal", "fire", "water", "electric", "grass",
                        "ice", "fighting", "poison", "ground", "flying", "psychic", "bug", "rock",
                        "ghost", "dragon", "dark", "steel", "fairy"};

        public static List<List<String>> getDefender(String type1, String type2) throws Exception {
            try {
                List<List<String>> chart1 = getDefender(type1);
                List<List<String>> chart2 = getDefender(type2);

                List<List<String>> combined = new ArrayList<>();
                combined.add(new ArrayList<String>());
                combined.add(new ArrayList<String>());
                combined.add(new ArrayList<String>());
                combined.add(new ArrayList<String>());
                combined.add(new ArrayList<String>());
                combined.add(new ArrayList<String>());

                for (int i = 0; i < chart1.size(); i++) {
                    for (String s : chart1.get(i)) {
                        int j = -1;
                        if (chart2.get(0).contains(s))
                            j = 0;
                        else if (chart2.get(1).contains(s))
                            j = 1;
                        else if (chart2.get(2).contains(s))
                            j = 2;
                        else if (chart2.get(3).contains(s))
                            j = 3;
                        else if (chart2.get(4).contains(s))
                            j = 4;
                        else if (chart2.get(5).contains(s))
                            j = 5;

                        if (j == -1)
                            throw new Exception(String.format("Type %s not found in list", s));

                        int index = -1;
                        if (i == 5 || j == 5)
                            index = 5;
                        else
                            index = i + (j - 2);

                        combined.get(index).add(s);
                    }
                }
                return combined;
            }
            catch (Exception e) {
                throw e;
            }
        }

        public static List<List<String>> getDefender(String type) throws Exception {
            double[] effectiveness;
            switch (type){
                case "normal":
                    effectiveness = new double[] {1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1};
                    break;
                case "fire":
                    effectiveness = new double[] {1, 0.5, 2, 1, 0.5, 0.5, 1, 1, 2, 1, 1, 0.5, 2, 1, 1, 1, 0.5, 0.5};
                    break;
                case "water":
                    effectiveness = new double[] {1, 0.5, 0.5, 2, 2, 0.5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0.5, 1};
                    break;
                case "electric":
                    effectiveness = new double[] {1, 1, 1, 0.5, 1, 1, 1, 1, 2, 0.5, 1, 1, 1, 1, 1, 1, 0.5, 1};
                    break;
                case "grass":
                    effectiveness = new double[] {1, 2, 0.5, 0.5, 0.5, 2, 1, 2, 0.5, 2, 1, 2, 1, 1, 1, 1, 1, 1};
                    break;
                case "ice":
                    effectiveness = new double[] {1, 2, 1, 1, 1, 0.5, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 2, 1};
                    break;
                case "fighting":
                    effectiveness = new double[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 0.5, 0.5, 1, 1, 0.5, 1, 2};
                    break;
                case "poison":
                    effectiveness = new double[] {1, 1, 1, 1, 0.5, 1, 0.5, 0.5, 2, 1, 2, 0.5, 1, 1, 1, 1, 1, 0.5};
                    break;
                case "ground":
                    effectiveness = new double[] {1, 1, 2, 0, 2, 2, 1, 0.5, 1, 1, 1, 1, 0.5, 1, 1, 1, 1, 1};
                    break;
                case "flying":
                    effectiveness = new double[] {1, 1, 1, 2, 0.5, 2, 0.5, 1, 0, 1, 1, 0.5, 2, 1, 1, 1, 1, 1};
                    break;
                case "psychic":
                    effectiveness = new double[] {1, 1, 1, 1, 1, 1, 0.5, 1, 1, 1, 0.5, 2, 1, 2, 1, 2, 1, 1};
                    break;
                case "bug":
                    effectiveness = new double[] {1, 2, 1, 1, 0.5, 1, 0.5, 1, 0.5, 2, 1, 1, 2, 1, 1, 1, 1, 1};
                    break;
                case "rock":
                    effectiveness = new double[] {0.5, 0.5, 2, 1, 2, 1, 2, 0.5, 2, 0.5, 1, 1, 1, 1, 1, 1, 2, 1};
                    break;
                case "ghost":
                    effectiveness = new double[] {0, 1, 1, 1, 1, 1, 0, 0.5, 1, 1, 1, 0.5, 1, 2, 1, 2, 1, 1};
                    break;
                case "dragon":
                    effectiveness = new double[] {1, 0.5, 0.5, 0.5, 0.5, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2};
                    break;
                case "dark":
                    effectiveness = new double[] {1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 0, 2, 1, 0.5, 1, 0.5, 1, 2};
                    break;
                case "steel":
                    effectiveness = new double[] {0.5, 2, 1, 1, 0.5, 0.5, 2, 0, 2, 0.5, 0.5, 0.5, 0.5, 1, 0.5, 1, 0.5, 0.5};
                    break;
                case "fairy":
                    effectiveness = new double[] {1, 1, 1, 1, 1, 1, 0.5, 2, 1, 1, 1, 0.5, 1, 1, 0, 0.5, 2, 1};
                    break;
                default:
                    throw new Exception("Unexpected Type Name");
            }

            try {
                return getListFromArray(effectiveness);
            }
            catch (Exception e) {
                throw e;
            }
        }

        public static List<List<String>> getAttacker(String type) throws Exception {
            double[] effectiveness;
            switch (type){
                case "normal":
                    effectiveness = new double[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0.5, 0, 1, 1, 0.5, 1};
                    break;
                case "fire":
                    effectiveness = new double[] {1, 0.5, 0.5, 1, 2, 2, 1, 1, 1, 1, 1, 2, 0.5, 1, 0.5, 1, 2, 1};
                    break;
                case "water":
                    effectiveness = new double[] {1, 2, 0.5, 1, 0.5, 1, 1, 1, 2, 1, 1, 1, 2, 1, 0.5, 1, 1, 1};
                    break;
                case "electric":
                    effectiveness = new double[] {1, 1, 2, 0.5, 0.5, 1, 1, 1, 0, 2, 1, 1, 1, 1, 0.5, 1, 1, 1};
                    break;
                case "grass":
                    effectiveness = new double[] {1, 0.5, 2, 1, 0.5, 1, 1, 0.5, 2, 0.5, 1, 0.5, 2, 1, 0.5, 1, 0.5, 1};
                    break;
                case "ice":
                    effectiveness = new double[] {1, 0.5, 0.5, 1, 2, 0.5, 1, 1, 2, 2, 1, 1, 1, 1, 2, 1, 0.5, 1};
                    break;
                case "fighting":
                    effectiveness = new double[] {2, 1, 1, 1, 1, 2, 1, 0.5, 1, 0.5, 0.5, 0.5, 2, 0, 1, 2, 2, 0.5};
                    break;
                case "poison":
                    effectiveness = new double[] {1, 1, 1, 1, 2, 1, 1, 0.5, 0.5, 1, 1, 1, 0.5, 0.5, 1, 1, 0, 2};
                    break;
                case "ground":
                    effectiveness = new double[] {1, 2, 1, 2, 0.5, 1, 1, 2, 1, 0, 1, 0.5, 2, 1, 1, 1, 2, 1};
                    break;
                case "flying":
                    effectiveness = new double[] {1, 1, 1, 0.5, 2, 1, 2, 1, 1, 1, 1, 2, 0.5, 1, 1, 1, 0.5, 1};
                    break;
                case "psychic":
                    effectiveness = new double[] {1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 0.5, 1, 1, 1, 1, 0, 0.5, 1};
                    break;
                case "bug":
                    effectiveness = new double[] {1, 0.5, 1, 1, 2, 1, 0.5, 0.5, 1, 0.5, 2, 1, 1, 0.5, 1, 2, 0.5, 0.5};
                    break;
                case "rock":
                    effectiveness = new double[] {1, 2, 1, 1, 1, 2, 0.5, 1, 0.5, 2, 1, 2, 1, 1, 1, 1, 0.5, 1};
                    break;
                case "ghost":
                    effectiveness = new double[] {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 0.5, 1, 1};
                    break;
                case "dragon":
                    effectiveness = new double[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 0.5, 0};
                    break;
                case "dark":
                    effectiveness = new double[] {1, 1, 1, 1, 1, 1, 0.5, 1, 1, 1, 2, 1, 1, 2, 1, 0.5, 1, 0.5};
                    break;
                case "steel":
                    effectiveness = new double[] {1, 0.5, 0.5, 0.5, 1, 2, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 0.5, 2};
                    break;
                case "fairy":
                    effectiveness = new double[] {1, 0.5, 1, 1, 1, 1, 2, 0.5, 1, 1, 1, 1, 1, 1, 2, 2, 0.5, 1};
                    break;
                default:
                    throw new Exception("Unexpected Type Name");
            }

            try {
                return getListFromArray(effectiveness);
            }
            catch (Exception e) {
                throw e;
            }
        }

        private static List<List<String>> getListFromArray(double[] effectiveness) throws Exception {
            List<List<String>> result = new ArrayList<List<String>>();
            result.add(new ArrayList<String>());
            result.add(new ArrayList<String>());
            result.add(new ArrayList<String>());
            result.add(new ArrayList<String>());
            result.add(new ArrayList<String>());
            result.add(new ArrayList<String>());

            for (int i = 0; i < effectiveness.length; i++) {
                if (effectiveness[i] == 2)
                    result.get(1).add(typeTitles[i]);
                else if (effectiveness[i] == 1)
                    result.get(2).add(typeTitles[i]);
                else if (effectiveness[i] == 0.5)
                    result.get(3).add(typeTitles[i]);
                else if (effectiveness[i] == 0)
                    result.get(5).add(typeTitles[i]);
                else throw new Exception("Unexpected Type Effectiveness Value");
            }

            return result;
        }
    }
}
