package org.sefaria.sefaria.TOCElements;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.sefaria.sefaria.R;
import org.sefaria.sefaria.Util;
import org.sefaria.sefaria.activities.TextActivity;
import org.sefaria.sefaria.database.Node;
import org.sefaria.sefaria.menu.MenuActivity;
import org.sefaria.sefaria.menu.MenuButton;
import org.sefaria.sefaria.menu.MenuButtonTab;
import org.sefaria.sefaria.menu.MenuElement;
import org.sefaria.sefaria.menu.MenuNode;
import org.sefaria.sefaria.menu.MenuState;
import org.sefaria.sefaria.menu.MenuSubtitle;

import java.util.ArrayList;
import java.util.List;



public class TOCGrid extends LinearLayout {

    private static final int HOME_MENU_OVERFLOW_NUM = 9;

    private Context context;
    private int numColumns;

    private List<TOCNumBox> overflowButtonList;

    private List<TOCElement> menuElementList;
    private List<TOCTab> TocTabList;

    private boolean hasTabs; //does current page tabs (eg with Talmud)
    private boolean limitGridSize;

    //the LinearLayout which contains the actual grid of buttons, as opposed to the tabs
    //which is useful for destroying the page and switching to a new tab
    private LinearLayout gridRoot;
    private LinearLayout tabRoot;

    private List<Node> tocRoots;

    private Util.Lang lang = Util.Lang.BI;
    private boolean flippedForHe; //are the views flipped for hebrew

    public TOCGrid(Context context,List<Node> tocRoots, int numColumns, boolean limitGridSize, Util.Lang lang) {
        super(context);
        this.tocRoots = tocRoots;
        this.context = context;
        this.numColumns = numColumns;
        this.limitGridSize = limitGridSize;

        init();
        setLang(lang);
    }

    private void init() {
        this.setOrientation(LinearLayout.VERTICAL);
        this.setPadding(10, 10, 10, 10);
        this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        this.gridRoot = new LinearLayout(context);
        gridRoot.setOrientation(LinearLayout.VERTICAL);
        gridRoot.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        gridRoot.setGravity(Gravity.CENTER);
        this.addView(gridRoot);


        this.overflowButtonList = new ArrayList<>();
        this.menuElementList = new ArrayList<>();
        this.TocTabList = new ArrayList<>();
        this.hasTabs = true;//lets assume for now... either with enough roots or with commentary
        this.flippedForHe = false;


        addTabsections(tocRoots);
        activateTab(0);//0 is default


    }


    private LinearLayout addRow() {
        LinearLayout ll = new LinearLayout(context);
        ll.setBackgroundResource(R.color.apocrypha);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        for (int i = 0; i < numColumns; i++) {
            ll.addView(new TOCNumBox(context));
        }
        gridRoot.addView(ll);
        return ll;
    }

    public void addNumGrid(Node mainNode) {
        List<Integer> chaps = mainNode.getChaps();
        if (chaps.size() == 0) return;

        int currNodeIndex = 0;

        for (int i = 0; i <= Math.ceil(chaps.size()/numColumns) && currNodeIndex < chaps.size(); i++) {
            LinearLayout linearLayout = addRow();

            for (int j = 0; j < numColumns && currNodeIndex < chaps.size();  j++) {
                //TOCNumBox mb = addElement(chaps.get(currNodeIndex),mainNode, linearLayout,j);
                TOCNumBox tocNumBox = new TOCNumBox(context,chaps.get(currNodeIndex), mainNode, lang);
                linearLayout.addView(tocNumBox);
                currNodeIndex++;
            }
        }
    }


    private void freshGridRoot(){
        if(gridRoot != null){
            gridRoot.removeAllViews();
        }
    }

    private void activateTab(TOCTab tocTab){
        for(TOCTab tempTocTab: TocTabList){
            tempTocTab.setActive(false);
        }

        tocTab.setActive(true);

        freshGridRoot();
        addNumGrid(tocTab.getNode());

    }



    private void activateTab(int num) {
        TOCTab tocTab = TocTabList.get(num);
        activateTab(tocTab);
    }

    private void addTabsections(List<Node> nodeList) {

        tabRoot = new LinearLayout(context);
        tabRoot.setOrientation(LinearLayout.HORIZONTAL);
        tabRoot.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        tabRoot.setGravity(Gravity.CENTER);
        this.addView(tabRoot, 0); //make sure it's on top

        for (int i=0;i<nodeList.size();i++) {
            //ns comment from menu
            //although generally this isn't necessary b/c the nodes come from menuState.getSections
            //this is used when rebuilding after memory dump and nodes come from setHasTabs()
            //

            Node node = nodeList.get(i);
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            inflater.inflate(R.layout.tab_divider_menu, tabRoot);

            TOCTab tocTab = new TOCTab(context,node,lang);
            tocTab.setOnClickListener(tabButtonClick);
            tabRoot.addView(tocTab);

            TocTabList.add(tocTab);

        }
    }

    private TOCNumBox addElement(MenuNode node, MenuNode sectionNode, LinearLayout ll, int childIndex) {
        ll.removeViewAt(childIndex);
        TOCNumBox mb = new TOCNumBox(context);
        mb.setOnClickListener(menuButtonClick);
        ll.addView(mb, childIndex);

        menuElementList.add(mb);

        return mb;


    }


    public void setLang(Util.Lang lang) {
        this.lang = lang;
        if ((lang == Util.Lang.HE && !flippedForHe) ||
                (lang == Util.Lang.EN && flippedForHe)) {

            flippedForHe = lang == Util.Lang.HE;
            flipViews(true);
        }
        for (TOCElement me : menuElementList) {
            me.setLang(lang);
        }

    }

    //used when switching languages or building a hebrew page
    //reverse the order of every row in the grid
    private void flipViews(boolean flipTabsAlso) {

        int numChildren;
        if (tabRoot != null && flipTabsAlso) {
            numChildren = tabRoot.getChildCount();
            for (int i = 0; i < numChildren; i++) {
                View tempView = tabRoot.getChildAt(numChildren - 1);
                tabRoot.removeViewAt(numChildren - 1);
                tabRoot.addView(tempView, i);
            }
        }

        for (int i = 0; i < gridRoot.getChildCount(); i++) {
            View v = gridRoot.getChildAt(i);
            if (v instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) v;
                numChildren = ll.getChildCount();
                for (int j = 0; j < numChildren - 1; j++) {
                    View tempView = ll.getChildAt(numChildren - 1);
                    ll.removeViewAt(numChildren - 1);
                    ll.addView(tempView, j);
                }


            }
        }
    }

    public Util.Lang getLang() { return lang; }


    /*
    public boolean getHasTabs() { return hasTabs; }

    //used when you're rebuilding after memore dump
    //you need to make sure that you add the correct tabs
    public void setHasTabs(boolean hasTabs) {
        this.hasTabs = hasTabs;
        addTabsections(tocRoots);
    }
    */
    public void goBack(boolean hasSectionBack, boolean hasTabBack) {
        //menuState.goBack(hasSectionBack, hasTabBack);

    }

    public void goHome() {
        ;//menuState.goHome();
    }

    public OnClickListener menuButtonClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            MenuButton mb = (MenuButton) v;
            //MenuState newMenuState = menuState.goForward(mb.getNode(), mb.getSectionNode());
            Intent intent;
            if (mb.isBook()) {
                intent = new Intent(context, TextActivity.class);
                //trick to destroy all activities beforehand
                //ComponentName cn = intent.getComponent();
                //Intent mainIntent = IntentCompat.makeRestartActivityTask(cn);
                //intent.putExtra("menuState", newMenuState);




                //jh
                //context.startActivity(intent);

            }else {
                intent = new Intent(context, MenuActivity.class);
                //intent.putExtra("menuState", newMenuState);
                intent.putExtra("hasSectionBack", mb.getSectionNode() != null);
                intent.putExtra("hasTabBack", hasTabs);


                ((Activity)context).startActivityForResult(intent, 0);
            }


        }
    };

    public OnClickListener moreButtonClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setVisibility(View.GONE);
            for (TOCNumBox mb : overflowButtonList) {
                mb.setVisibility(View.VISIBLE);
            }

        }
    };

    public OnClickListener tabButtonClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            TOCTab tocTab = (TOCTab) view;
            activateTab(tocTab);
        }
    };
}
