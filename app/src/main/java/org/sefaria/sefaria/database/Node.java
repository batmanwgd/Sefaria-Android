package org.sefaria.sefaria.database;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.sefaria.sefaria.MyApp;
import org.sefaria.sefaria.Util;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Node{ //TODO implements  Parcelable


    public final static int NODE_TYPE_BRANCH = 1;
    public final static int NODE_TYPE_TEXTS = 2;
    public final static int NODE_TYPE_REFS = 3;


    public final static int NID_NON_COMPLEX = -3;
    public  final static int NID_NO_INFO = -1;
    public final static int NID_CHAP_NO_NID = -4;

    private int nid;
    private int bid;
    private int parentNodeID;
    private int nodeType;
    private int siblingNum;
    private String enTitle;
    private String heTitle;
    private String [] sectionNames;
    private String [] heSectionNames;
    private int structNum;
    private int textDepth;
    private int startTid;
    private int endTid;
    private String extraTids;

    private List<Node> children;
    //private List<Integer> chaps;
    private List<Text> textList;
    private Node parent = null;

    /**
     * These booleans are set in setFlagsFromNodeType()
     */
    private boolean isTextSection = false;
    private boolean isGridItem = false;
    private boolean isComplex = false;
    private boolean isRef = false;

    //private int [] levelsOfNode = new int [] {};
    int gridNum = -1;
    public int getGridNum(){ return gridNum;}

    private static Map<Integer,Node> allSavedNodes = new HashMap<Integer, Node>();

    public static Node getSavedNode(int hash){
        return allSavedNodes.get(hash);
    }
    public static void saveNode(Node node){
        allSavedNodes.put(node.hashCode(),node);
    }


    private static String NODE_TABLE = "Nodes";


    public Node(){
        children = new ArrayList<>();
        //chaps = new ArrayList<>();
        nid = NID_NO_INFO;
        parentNodeID = NID_NO_INFO;
    }

    public Node(Book book){
        children = new ArrayList<>();
        //chaps = new ArrayList<>();
        nid = NID_NO_INFO;
        parentNodeID = NID_NO_INFO;

        bid = book.bid;
        sectionNames = book.sectionNamesL2B;
        heSectionNames = book.heSectionNamesL2B;
        textDepth = book.textDepth;
        enTitle = book.title;
        heTitle = book.heTitle;
    }


    public  Node (Cursor cursor){
        children = new ArrayList<>();
        nid = NID_NO_INFO;
        parentNodeID = NID_NO_INFO;
        //chaps = new ArrayList<>();
        getFromCursor(cursor);

    }

    /***
     *  Returns full description of current text.
     *  For example, includes Genesis 2
     *  //TODO actually create function (maybe use headers from beatmidrash)
     * @param lang
     * @return
     */
    public String getWholeTitle(Util.Lang lang){
        return getTitle(lang);
    }

    /**
     * use lang from Util.HE or EN (maybe or BI)
     * @param lang
     */
    public String getTitle(Util.Lang lang){
        if(Util.Lang.EN == lang)
            return enTitle;
        else if(Util.Lang.HE == lang)
            return heTitle;
        else if(Util.Lang.BI == lang) {
            return enTitle + " - " + heTitle;
        }
        else{
            Log.e("Node", "wrong lang num");
            return "";
        }
    }

    public static Node getFirstDescendant(Node node){
        if(node.getChildren().size() > 0){
            node = node.getChildren().get(0);
            return getFirstDescendant(node);
        }else{
            return node;
        }
    }

    /**
     * if (children.size() == 0)
     *      this is a leaf and you need to check
     *
     * @return childrenl
     */
    public List<Node> getChildren(){
        if(children == null)
            children = new ArrayList<>();
        return children;
    }

    /**
     * chaps.size() == 0 when there's no chaps within it. If there's no children, this means that this Node contains Texts
     *
     public  List<Integer> getChaps(){
     if(chaps == null)
     chaps = new ArrayList<>();
     return chaps;
     }
     * @return chaps
     */




    private void addSectionNames(){
        //TODO move to create SQL
        if(sectionNames.length > 0)
            return;
        String sectionStr = "[";
        for(int i=0;i<textDepth;i++) {
            sectionStr += "Section" + i;
            if(i < textDepth-1)
                sectionStr += ",";
        }
        sectionStr += "]";
        //Log.d("Node",sectionStr);
        sectionNames = Util.str2strArray(sectionStr);
        heSectionNames = sectionNames;

    }

    private static final int IS_COMPLEX = 2;
    private static final int IS_TEXT_SECTION = 4;
    private static final int IS_GRID_ITEM = 8;
    private static final int IS_REF = 16;
    private void setFlagsFromNodeType(){
        if(nodeType == 0)
            Log.e("Node", "Node.setFlagsFromNodeType: nodeType == 0. I don't know anything");
        isComplex = (nodeType & IS_COMPLEX) != 0;
        isTextSection = (nodeType & IS_TEXT_SECTION) != 0;
        isGridItem = (nodeType & IS_GRID_ITEM) != 0;
        isRef = (nodeType & IS_REF) != 0;
        if(isComplex){
            gridNum = siblingNum + 1;
            //TODO this needs to be more complex for when there's: Intro,1,2,3,conclusion
        }
    }

    public boolean isComplex(){
        if(this.nid == NID_NON_COMPLEX || true) //TODO remmove || true
            return false;
        else
            return isComplex;
    }

    public boolean isTextSection(){ return isTextSection; }
    public boolean isGridItem() { return isGridItem; }
    public boolean isRef(){ return isRef;}

    private void getFromCursor(Cursor cursor){
        try{
            nid = cursor.getInt(0);
            bid = cursor.getInt(1);
            parentNodeID = cursor.getInt(2);
            nodeType = cursor.getInt(3);
            siblingNum = cursor.getInt(4);
            enTitle = cursor.getString(5);
            heTitle = cursor.getString(6);
            sectionNames = Util.str2strArray(cursor.getString(7));
            heSectionNames = Util.str2strArray(cursor.getString(8));
            structNum = cursor.getInt(9);
            textDepth = cursor.getInt(10);
            startTid = cursor.getInt(11);
            endTid = cursor.getInt(12);
            extraTids = cursor.getString(13);

            setFlagsFromNodeType();
            addSectionNames();
        }
        catch(Exception e){
            MyApp.sendException(e);
            Log.e("Node", e.toString());
            nid = 0;
            return;
        }
    }

    private void addChapChild(int chapNum){
        Node node = new Node();
        node.nodeType = 0;
        node.bid = bid;
        node.isGridItem = true;
        node.isTextSection = true;
        node.isRef = false;
        node.isComplex = isComplex;
        node.nodeType = 0;
        node.gridNum = chapNum;
        node.siblingNum = -1;
        node.enTitle = node.heTitle =  "";
        node.heSectionNames = node.sectionNames = sectionNames;
        node.parentNodeID = nid;
        node.textDepth = 2;
        node.structNum = structNum;
        node.nid = NID_CHAP_NO_NID;
        addChild(node);
    }

    private void addChild(Node node){
        this.children.add(node);
        node.parent = this;
        if(node.parentNodeID == NID_NO_INFO)
            node.parentNodeID = this.nid;
        if(node.siblingNum == -1){
            node.siblingNum = this.children.size() -1;
        }else if(node.siblingNum != this.children.size() -1) {
            //TODO make sure the order is correct
            Log.e("Node", "wrong sibling num");
        }
    }

    /**
     * just for debugging
     * @param node
     */
    private static void showTree(Node node){
        node.log();
        if(node.children.size() == 0) {
            //Log.d("Node showtree", "chaps: "+ node.chaps);
            return;
        }
        else{
            //Log.d("Node", "node " + node.nid + " children: ");
            for(int i=0;i<node.children.size();i++) {
                node.children.get(i).log();
            }
            //Log.d("node", "... end of chilren list");
            for(int i=0;i<node.children.size();i++){
                showTree(node.children.get(i));
            }

        }
    }

    /**
     * converts complex nodes list into a complete tree
     * @param nodes
     * @return root
     * @throws API.APIException
     */
    private static Node convertToTree(List<Node> nodes) throws API.APIException{
        Node root = null;
        //TODO for each struct
        int startID = -1;
        for(int i =0;i<nodes.size();i++){
            Node node = nodes.get(i);

            if(startID < 0)
                startID = node.nid;

            if(node.parentNodeID == 0){
                if(root == null)
                    root = node;
                else
                    Log.e("Node", "Root already taken!!");
            }else{
                Node node2 = nodes.get(node.parentNodeID - startID);
                if(node2.nid == node.parentNodeID)
                    node2.addChild(node);
                else{
                    Log.e("Node","Parent in wrong spot");
                    for(int j=0;j<nodes.size();j++){
                        node2 = nodes.get(j);
                        if(node2.nid == node.parentNodeID) {
                            node2.addChild(node);
                            break;
                        }
                    }
                }

                // add chap count (if it's a leaf with 2 or more levels):
                if(node.textDepth >= 2 && node.nodeType == NODE_TYPE_TEXTS) {
                    node.setAllChaps(true);
                }
            }

        }

        return root;
    }


    private void setAllChaps(boolean useNID) throws API.APIException {
        //if(true) return;
        Log.d("Node","starting setAllChap()");
        if(textDepth < 2){
            Log.e("Node", "called setAllChaps with too low texdepth" + this.toString());
            return;
        }

        Database2 dbHandler = Database2.getInstance();
        SQLiteDatabase db = dbHandler.getReadableDatabase();


        String levels = "";
        for(int i=textDepth;i>1;i--){
            levels += "level" + i;
            if(i > 2)
                levels += ",";
        }
        //Log.d("Node", "node:" + this.toString());

        String sql = "SELECT DISTINCT " + levels + " FROM "+ Text.TABLE_TEXTS;

        if(useNID)  sql += " WHERE bid = " + this.bid + " AND  parentNode = " + this.nid;
        else        sql += " WHERE bid = " + this.bid;

        sql += " ORDER BY  " + levels;

        if(!useNID) nodeType = NODE_TYPE_TEXTS;
        //Log.d("Node", "sql: " + sql);
        Node tempNode = null;
        int lastLevel3 = 0;
        int originalNodeType = this.nodeType;

        if(API.useAPI()){
            ;
            //chapList = API.getAllChaps(Book.getTitle(bid),levels);
            //TODO add complex text API stuff
        }

        try{
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                do {
                    if(textDepth == 2) {
                        //chaps.add(cursor.getInt(0));
                        addChapChild(cursor.getInt(0));
                    }else if(textDepth == 3){
                        this.nodeType = NODE_TYPE_BRANCH;//TODO see if this needs a different nodeType number
                        int level3 = cursor.getInt(0);
                        if(level3 != lastLevel3 || tempNode == null){
                            lastLevel3 = level3;
                            tempNode = new Node();
                            tempNode.enTitle = this.sectionNames[2] + " " + level3;
                            tempNode.heTitle = this.heSectionNames[2] + " " + Util.int2heb(level3);
                            tempNode.sectionNames = Arrays.copyOfRange(this.sectionNames,0,2);
                            tempNode.heSectionNames = Arrays.copyOfRange(this.heSectionNames,0,2);
                            tempNode.textDepth = this.textDepth -1;
                            tempNode.nodeType = originalNodeType;
                            tempNode.isComplex = isComplex();
                            tempNode.isRef = false;
                            tempNode.isGridItem = false;
                            tempNode.isTextSection = false;
                            this.addChild(tempNode);
                        }
                        tempNode.addChapChild(cursor.getInt(1));
                        //TODO extend to more than 3 levels
                    }
                    else{
                        Log.e("Node", "add 4 or more levels");//TODO
                    }
                } while (cursor.moveToNext());
            }
        }catch(Exception e){
            Log.e("Node", e.toString());
        }

        Log.d("Node","finishing getAllChap()");

        return;
    }


    /**
     *  Get texts for complex texts
     *
     * @return textList
     * @throws API.APIException
     */
    public List<Text> getTexts() throws API.APIException{
        //TODO check for chapters (if it was supposed to pass a chapNum)

        if(textList == null) {
            textList = Text.get(getBid(), getLevels(), false);//// TODO: 12/30/2015 nid
        }
        return textList;
    }

    /**
     * get Texts for non-complex texts. Given a chapter  number.
     * @param chapNum
     * @return textList
     * @throws API.APIException
     */
    public List<Text> getTexts(int chapNum) throws API.APIException{
        //TODO check if it wasn't supposed to check for chapters
        List<Text> texts = new ArrayList<>();
        Log.d("Node", "" + this);
        if(!this.isComplex()){
            //TODO make work for more than 2 levels!!!!
            int [] levels = {0,chapNum};

            texts =  Text.get(bid,levels,false);
        }
        else if(this.nodeType == NODE_TYPE_TEXTS){

            //TODO error check bid and maybe levels

            //TODO this has to figure out if we're talking about nid (complex text) or not.
        }else{
            Log.d("Node","NODE_TYPE:" + this.nodeType);
        }
        return texts;
    }


    public int getBid(){
        return bid;
    }

    /**
     * create an array describing the levels:
     * ex. chap 4 and verse 7 would be {7,3}
     * return levels
     */
    public int [] getLevels(){
        int levels [] = new int [] {1,2};//TODO based on logic
        return levels;
    }


    public static List<Node> getRoots(Book book) throws API.APIException {
        return getRoots(book,true);
    }

    private static List<Node> getRoots(Book book, boolean addMoreh) throws API.APIException{
        Database2 dbHandler = Database2.getInstance();
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        Cursor cursor = db.query(NODE_TABLE, null, "bid" + "=?",
                new String[]{String.valueOf(book.bid)}, null, null, "structNum,_id", null); //structNum, parentNode, siblingNum
        Node root;

        int lastStructNum = -1;
        List<List<Node>> allNodeStructs = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()){
            do {
                Node node = new Node(cursor);
                //node.log();
                if(lastStructNum != node.structNum){
                    allNodeStructs.add(new ArrayList<Node>());
                    lastStructNum = node.structNum;
                    Log.d("Node", "On structNum" + node.structNum);
                }
                allNodeStructs.get(allNodeStructs.size()-1).add(node);
                //nodes.add(node);
            } while (cursor.moveToNext());
        }
        List<Node> allRoots = new ArrayList<>();
        //TODO need to add way if the main text is regular but has alt... make it always try to add it.
        /**
         * this is for the rigid structure
         */
        root = new Node(book);
        root.nid = NID_NON_COMPLEX;
        root.setAllChaps(false);
        if(root.getChildren().size()>0) {
            allRoots.add(root);
        }

        for(int i=0;i<allNodeStructs.size();i++) {
            List<Node> nodes = allNodeStructs.get(i);
            if (nodes.size() > 0) {
                //Complex text combining nodes into root
                root = convertToTree(nodes);
            }
            allRoots.add(root);
            showTree(root);
        }


        if(addMoreh){//TODO remove only for testing alt structures
            allRoots.add(getRoots(new Book("Orot"),false).get(0)); //has complex texts
            allRoots.add(getRoots(new Book("Sefer Tomer Devorah"),false).get(0));//has textDepth == 3

        }


        return allRoots;
    }

    @Override
    public String toString() {
        return nid + "," + bid + "," + enTitle + " " + heTitle + "," + Util.array2str(sectionNames) + "," + Util.array2str(heSectionNames) + "," + structNum + "," + textDepth + "," + startTid + "," + endTid + "," + extraTids;
    }

    public void log(){
        Log.d("Node", this.toString());
    }


    /*
    //TODO
    //PARCELABLE------------------------------------------------------------------------

    public static final Parcelable.Creator<Node> CREATOR
            = new Parcelable.Creator<Node>() {
        public Node createFromParcel(Parcel in) {
            return new Node(in);
        }

        public Node[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(bid);
        dest.writeInt(commentsOn);
        dest.writeStringArray(sectionNames);
        dest.writeStringArray(sectionNamesL2B);
        dest.writeStringArray(heSectionNamesL2B);
        dest.writeStringArray(categories);
        dest.writeInt(textDepth);
        dest.writeInt(wherePage);
        dest.writeString(title);
        dest.writeString(heTitle);
        dest.writeInt(languages);
    }

    private Book(Parcel in) {
        bid = in.readInt();
        commentsOn = in.readInt();
        sectionNames = in.createStringArray();
        sectionNamesL2B = in.createStringArray();
        heSectionNamesL2B = in.createStringArray();
        categories = in.createStringArray();

        textDepth = in.readInt();
        wherePage = in.readInt();
        title = in.readString();
        heTitle = in.readString();
        languages = in.readInt();
    }
    */

}
