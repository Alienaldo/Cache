import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.util.*;
import java.math.BigDecimal;


public class CCacheSim extends JFrame implements ActionListener{

    private JPanel panelTop, panelLeft, panelRight, panelBottom;
    private JButton execStepBtn, execAllBtn, fileBotton;
    private JComboBox ucsBox, bsBox, wayBox, replaceBox, prefetchBox, writeBox, allocBox,dCacheBox,iCacheBox;
    private JFileChooser fileChoose;

    private JLabel labelTop,labelLeft,rightLabel,bottomLabel,fileLabel,fileAddrBtn, stepLabel1, stepLabel2,
            csLabel, bsLabel, wayLabel, replaceLabel, prefetchLabel, writeLabel, allocLabel,dCacheLabel,iCacheLabel;
    private JLabel [][] results;


    //参数定义
    private String cachesize[] = { "2KB", "4KB", "8KB", "16KB", "32KB", "64KB","128KB", "256KB","512KB", "1MB"};
    private String dcachesize[] = {"1KB","2KB","4KB","8KB","16KB","32KB","64KB","128KB","256KB","512KB"};
    private String icachesize[] = {"1KB","2KB","4KB","8KB","16KB","32KB","64KB","128KB","256KB","512KB"};
    private String blocksize[] = { "16B", "32B", "64B", "128B", "256B" };
    private String way[] = { "直接映象", "2路", "4路", "8路", "16路", "32路" };
    private String replace[] = { "LRU", "FIFO", "RAND" };
    private String pref[] = { "不预取", "不命中预取" };
    private String write[] = { "写回法", "写直达法" };
    private String alloc[] = { "按写分配", "不按写分配" };
    private String typename[] = { "读数据", "写数据", "读指令" };
    private String hitname[] = {"不命中", "命中" };

    //右侧结果显示
    private String rightLable[]={"访问总次数：","读指令次数：","读数据次数：","写数据次数："};

    //打开文件
    private File file=null;

    //分别表示左侧几个下拉框所选择的第几项，索引从 0 开始
    private int ucsIndex, dcsIndex, icsIndex, bsIndex, wayIndex, replaceIndex, prefetchIndex, writeIndex, allocIndex;
    private int mucsIndex,mdcsIndex,micsIndex,mbsIndex,mwayIndex,mreplaceIndex,mprefetchIndex,mwriteIndex,mallocIndex;

    //其它变量定义
    //...
    private static int max=2000000;
    private String [] Buffer_in=new String[max];
    private int buffer_size=0;
    private int instruction_num;
    private int ip;
//    以下为模拟结果变量
    private int totalAccessTimes,totalNoHitTimes;
    double totalNoHitRate;
    private int readInsTimes,readInsNoHitTimes;
    double readInsNoHitRate;
    private int readDataTimes,readDataNoHitTimes;
    double readDataNoHitRate;
    private int writeDataTimes,writeDataNoHitTimes;
    double writeDataNoHitRate;

    private JLabel [][] resultLabeltag=new JLabel[4][3];
    private JLabel [][] resultDatatag=new JLabel[4][3];
    private String [][] resultData={
            {"0","0","0.00%"},
            {"0","0","0.00%"},
            {"0","0","0.00%"},
            {"0","0","0,00%"}
    };
    private String [][] resultMatrix=new String[4][3];
    private String [][] resultLabelText={
            {"访问总次数:","不命中次数:","不命中率:"},
            {"读指令次数:","不命中次数:","不命中率:"},
            {"读数据次数:","不命中次数:","不命中率:"},
            {"写数据次数:","不命中次数:","不命中率:"}
    };
    private int hit;
    JLabel accessType,accessTypeData,address,addressData,blockNum,blockNumData;
    JLabel blockInAddr,blockInAddrData,index,indexData,hitInfo,hitInfoData,tag,tagData;
    JRadioButton uCacheButton,sCacheButton;
    private Cache ucache,dcache,icache;
    private Instruction [] instruction;
    private int cacheType;
    private boolean change_imm=false;
    private class Instruction{
        int opt;
        int tag;
        int index;
        int blockinaddr;
        int blockaddr;
        int addr;
        public Instruction(String opt,String addr){
            String b_addr;
            this.opt=Integer.parseInt(opt);
            this.addr=Integer.parseInt(addr,16);
            b_addr=binaryToHex(addr);
            int b_addr_length=b_addr.length();
            if(cacheType==0){
                this.tag=Integer.parseInt(b_addr.substring(0,32-ucache.blockoffset-ucache.groupoffset),2);
                index=Integer.parseInt(b_addr.substring(32-ucache.blockoffset-ucache.groupoffset,32-ucache.blockoffset),2);
                blockaddr=Integer.parseInt(b_addr.substring(0,32-ucache.blockoffset),2);
                blockinaddr=Integer.parseInt(b_addr.substring(32-ucache.blockoffset),2);
            }else if(cacheType==1){
                if(this.opt==0||this.opt==1){
                    this.tag=Integer.parseInt(b_addr.substring(0,32-dcache.blockoffset-dcache.groupoffset),2);
                    index=Integer.parseInt(b_addr.substring(32-dcache.blockoffset-dcache.groupoffset,32-dcache.blockoffset),2);
                    blockaddr=Integer.parseInt(b_addr.substring(0,32-dcache.blockoffset),2);
                    blockinaddr=Integer.parseInt(b_addr.substring(32-dcache.blockoffset),2);
                }else if(this.opt==2){
                    this.tag=Integer.parseInt(b_addr.substring(0,32-icache.blockoffset-icache.groupoffset),2);
                    index=Integer.parseInt(b_addr.substring(32-icache.blockoffset-icache.groupoffset,32-icache.blockoffset),2);
                    blockaddr=Integer.parseInt(b_addr.substring(0,32-icache.blockoffset),2);
                    blockinaddr=Integer.parseInt(b_addr.substring(32-icache.blockoffset),2);
                }
            }
        }
        public void description(){
            System.out.println("opt: "+opt);
            System.out.println("tag: "+tag);
            System.out.println("index: "+index);
            System.out.println("blockinaddr: "+blockinaddr);
        }
    }
    private String binaryToHex(String s){
        StringBuffer b=new StringBuffer("");
        int zero_num,binary_s;
        StringBuffer c=new StringBuffer("");
        binary_s=Integer.parseInt(s,16);
        b.append(Integer.toBinaryString(binary_s));
        zero_num=32-b.length();
        for(int i=0;i<zero_num;i++)
        {
            c.append("0");
        }
        b.insert(0,c);
        return b.toString();
    }
    private class CacheBlock{
        int tag;
        boolean dirty;
        int count;
        long time;
        public CacheBlock(int tag){
            this.tag=tag;
            dirty=false;
            count=-1;
            time=-1L;
        }
    }
    private class Cache{
        int cachesize,blocksize;
        int blocknumincache,blocknumingroup,groupnum;
        int blockoffset,groupoffset;
        CacheBlock [][] cache;
        public Cache(int cachesize,int blocksize){
            this.cachesize=cachesize;
            this.blocksize=blocksize;
            blocknumincache=cachesize/blocksize;
            blocknumingroup=(int)Math.pow((double)2,mwayIndex);
            groupnum=blocknumincache/blocknumingroup;
            cache=new CacheBlock[groupnum][blocknumingroup];
            for(int i=0;i<groupnum;i++){
                for(int j=0;j<blocknumingroup;j++){
                    cache[i][j]=new CacheBlock(-1);
                }
            }
            blockoffset=(int)(Math.log((double)blocksize)/Math.log((double)2));
            groupoffset=(int)(Math.log((double)groupnum)/Math.log((double)2));
        }
        public int read(int tag,int index,int blockinaddr){
            int hit=0;
            for(int i=0;i<blocknumingroup;i++){
                if(cache[index][i].tag==tag){
                    hit=1;
                    cache[index][i].count=ip%instruction_num;
                    break;
                }
            }
            if(hit==0){
                replacecacheblock(tag,index,blockinaddr);
            }
            return(hit);
        }
        public int write(int tag,int index,int blockinaddr) {
            int hit = 0;
            for (int i = 0; i < blocknumingroup; i++) {
                if (cache[index][i].tag == tag) {
                    hit = 1;
                    cache[index][i].dirty = true;
                    cache[index][i].count=ip%instruction_num;
                    if(mwriteIndex==0){//写回法

                    }else if(mwriteIndex==1){//写直达法 访问内存次数加1
                        cache[index][i].dirty=false;
                    }
                    break;
                }
            }
            if(hit==0){
                if(mallocIndex==0){//按写分配
                    replacecacheblock(tag,index,blockinaddr);
                    write(tag,index,blockinaddr);
                }else if(mallocIndex==1){//不按写分配 内存访问次数加1

                }
            }
            return hit;
        }
        public void replacecacheblock(int tag,int index,int blockinaddr){
            int replace_index=0;
            if(mreplaceIndex==0){//LRU
                for(int i=1;i<blocknumingroup;i++){
                    if(cache[index][i].count<cache[index][replace_index].count){
                        replace_index=i;
                    }
                }
            }else if (mreplaceIndex==1){//FIFO
                for(int i=1;i<blocknumingroup;i++){
                    if(cache[index][i].time<cache[index][replace_index].time){
                        replace_index=i;
                    }
                }
            }else if (mreplaceIndex==2){//RAND
                Random r=new Random();
                replace_index=r.nextInt()/blocknumingroup;
            }
            if(mwriteIndex==0&&cache[index][replace_index].dirty){//写回法 写内存时间加1

            }
            cache[index][replace_index].count=ip%instruction_num;
            cache[index][replace_index].time=ip%instruction_num;
            cache[index][replace_index].dirty=false;
            cache[index][replace_index].tag=tag;
        }
        public void discription(){
            System.out.println("cachesize: "+cachesize);
            System.out.println("blocksize: "+blocksize);
            System.out.println("blocknumincache: "+blocknumincache);
            System.out.println("blocknumingroup: "+blocknumingroup);
            System.out.println("groupnum: "+groupnum);
            System.out.println("blockoffset: "+blockoffset);
            System.out.println("groupoffset: "+groupoffset);
        }
    }
    /*
     * 构造函数，绘制模拟器面板
     */
    public CCacheSim(){
        super("Cache Simulator");
        fileChoose = new JFileChooser();
        draw();
    }
    private class dinFilter extends javax.swing.filechooser.FileFilter {
        public boolean accept(File pathname) {
            if (pathname.isDirectory()) return true;
            else if (pathname.getName().toLowerCase().endsWith(".din")) return true;
            return false;
        }

        public String getDescription() {
            return ".din";
        }
    }
    //响应事件，共有三种事件：
    //   1. 执行到底事件
    //   2. 单步执行事件
    //   3. 文件选择事件
    public void actionPerformed(ActionEvent e){

        if (e.getSource() == execAllBtn) {
            simExecAll();
            allValueToMatrix();
            freshUI();
            addUI(false);
        }
        if (e.getSource() == execStepBtn) {
            simExecStep();
            allValueToMatrix();
            freshUI();
            addUI(true);
        }
        if (e.getSource() == fileBotton){
            buffer_size=0;
            fileChoose.setCurrentDirectory(new File(".\\instructions\\"));
            fileChoose.setFileFilter(new dinFilter());
            int fileOver = fileChoose.showOpenDialog(null);
            if (fileOver == 0) {
                String path = fileChoose.getSelectedFile().getAbsolutePath();
                fileAddrBtn.setText(path);
                file = new File(path);
                readFile();
                initialization();
            }
        }
    }
    public void initialization(){
        initCache();
        decodeIns();
    }
    public void freshUI(){
        for (int i=0; i<4; i++) {
            for(int j=0;j<3;j++){
                if(j!=2){
                    resultDatatag[i][j].setText(resultData[i][j]);
                }else{
                    resultDatatag[i][j].setText(String.format("%.2f",Double.parseDouble(resultData[i][j]))+"%");
                }

            }
        }
    }
    public void addUI(boolean visiable){
        if(visiable){
            accessTypeData.setText(typename[instruction[ip-1].opt]);
            addressData.setText(""+instruction[ip-1].addr);
            tagData.setText(""+instruction[ip-1].tag);
            blockNumData.setText(""+instruction[ip-1].blockaddr);
            blockInAddrData.setText(""+instruction[ip-1].blockinaddr);
            indexData.setText(""+instruction[ip-1].index);
            hitInfoData.setText(hitname[hit]);

            accessType.setVisible(true);
            accessTypeData.setVisible(true);
            address.setVisible(true);
            addressData.setVisible(true);
            tag.setVisible(true);
            tagData.setVisible(true);
            blockNum.setVisible(true);
            blockNumData.setVisible(true);
            blockInAddr.setVisible(true);
            blockInAddrData.setVisible(true);
            index.setVisible(true);
            indexData.setVisible(true);
            hitInfo.setVisible(true);
            hitInfoData.setVisible(true);
        }else{
            accessType.setVisible(false);
            accessTypeData.setVisible(false);
            address.setVisible(false);
            addressData.setVisible(false);
            tag.setVisible(false);
            tagData.setVisible(false);
            blockNum.setVisible(false);
            blockNumData.setVisible(false);
            blockInAddr.setVisible(false);
            blockInAddrData.setVisible(false);
            index.setVisible(false);
            indexData.setVisible(false);
            hitInfo.setVisible(false);
            hitInfoData.setVisible(false);
        }
    }
    /*
     * 初始化 Cache 模拟器
     */
    //mcsIndex,mbsIndex,mwayIndex,mreplaceIndex,mprefetchIndex,mwriteIndex,mallocIndex;
    public void initCache() {
        mbsIndex=bsIndex;
        mwayIndex=wayIndex;
        mreplaceIndex=replaceIndex;
        mprefetchIndex=prefetchIndex;
        mwriteIndex=writeIndex;
        mallocIndex=allocIndex;
        if(cacheType==0){
            mucsIndex=ucsIndex;
            ucache=new Cache((int)(2*1024*Math.pow((double)2,mucsIndex)),(int)(16*Math.pow((double)2,mbsIndex)));
        }else if(cacheType==1){
            micsIndex=icsIndex;
            mdcsIndex=dcsIndex;
            dcache=new Cache((int)(1024*Math.pow((double)2,mdcsIndex)),(int)(16*Math.pow((double)2,mbsIndex)));
            icache=new Cache((int)(1024*Math.pow((double)2,micsIndex)),(int)(16*Math.pow((double)2,mbsIndex)));
        }
        ip=0;
        initResult();
    }
    public void initResult(){
        totalAccessTimes=0;
        totalNoHitTimes=0;
        totalNoHitRate=0;
        readInsTimes=0;
        readInsNoHitTimes=0;
        readInsNoHitRate=0;
        readDataTimes=0;
        readDataNoHitTimes=0;
        readDataNoHitRate=0;
        writeDataTimes=0;
        writeDataNoHitTimes=0;
        writeDataNoHitRate=0;
    }
    /*
     * 将指令和数据流从文件中读入
     */
    public int readFile() {
        try{
            Scanner scann = new Scanner(file);
            for(int i=0;scann.hasNextLine();i++){
                Buffer_in[i]=scann.nextLine();
                buffer_size++;
            }
        }catch(Exception e){
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
    public void decodeIns(){
        instruction_num=buffer_size;
        instruction=new Instruction[instruction_num];
        for(int i=0;i<instruction_num;i++){
            String [] pieces=Buffer_in[i].split(" |\t");
            instruction[i]=new Instruction(pieces[0],pieces[1]);
        }
    }
    public void print(){
        for(int i=0;i<buffer_size;i++){
            System.out.println(Buffer_in[i]);
        }
    }

//    int totalAccessTimes,totalnoHitTimes;
//    double totalnoHitRate;
//    private int readInsTimes,readInsNoHitTimes;
//    double readInsNoHitRate;
//    private int readDataTimes,readDataNoHitTimes;
//    double readDataNoHitRate;
//    private int writeDataTimes,writeDataNoHitTimes;
//    double writeDataNoHitRate;
    /*
     * 模拟单步执行
     */
    public void simExecStep() {
        if(file!=null){
            if(cacheType==0){
                if(ip==0){
                    initResult();
                    initCache();
                }
                int opt=instruction[ip].opt;
                int tag=instruction[ip].tag;
                int index=instruction[ip].index;
                int blockinaddr=instruction[ip].blockinaddr;
                if(opt==0){//读数据
                    hit=ucache.read(tag,index,blockinaddr);
                    readDataTimes++;
                    if(hit==1){

                    }else{
                        readDataNoHitTimes++;
                    }
                    readDataNoHitRate=((double)readDataNoHitTimes/(double)readDataTimes)*100;
                }else if(opt==1){//写数据
                    hit=ucache.write(tag,index,blockinaddr);
                    writeDataTimes++;
                    if(hit==1){

                    }else{
                        writeDataNoHitTimes++;
                    }
                    writeDataNoHitRate=((double)writeDataNoHitTimes/(double)writeDataTimes)*100;
                }else if(opt==2){//取指令
                    hit=ucache.read(tag,index,blockinaddr);
                    readInsTimes++;
                    if(hit==1){

                    }else{
                        readInsNoHitTimes++;
                    }
                    readInsNoHitRate=((double)readInsNoHitTimes/(double)readInsTimes)*100;
                }
                totalAccessTimes=readDataTimes+writeDataTimes+readInsTimes;
                totalNoHitTimes=readDataNoHitTimes+writeDataNoHitTimes+readInsNoHitTimes;
                totalNoHitRate=((double)totalNoHitTimes/(double)totalAccessTimes)*100;
                ip=(ip+1)%instruction_num;
            }else if(cacheType==1){
                if(ip==0){
                    initResult();
                    initCache();
                }
                int opt=instruction[ip].opt;
                int tag=instruction[ip].tag;
                int index=instruction[ip].index;
                int blockinaddr=instruction[ip].blockinaddr;
                if(opt==0){//读数据
                    hit=dcache.read(tag,index,blockinaddr);
                    readDataTimes++;
                    if(hit==1){

                    }else{
                        readDataNoHitTimes++;
                    }
                    readDataNoHitRate=((double)readDataNoHitTimes/(double)readDataTimes)*100;
                }else if(opt==1){//写数据
                    hit=dcache.write(tag,index,blockinaddr);
                    writeDataTimes++;
                    if(hit==1){

                    }else{
                        writeDataNoHitTimes++;
                    }
                    writeDataNoHitRate=((double)writeDataNoHitTimes/(double)writeDataTimes)*100;
                }else if(opt==2){//取指令
                    hit=icache.read(tag,index,blockinaddr);
                    readInsTimes++;
                    if(hit==1){

                    }else{
                        readInsNoHitTimes++;
                    }
                    readInsNoHitRate=((double)readInsNoHitTimes/(double)readInsTimes)*100;
                }
                totalAccessTimes=readDataTimes+writeDataTimes+readInsTimes;
                totalNoHitTimes=readDataNoHitTimes+writeDataNoHitTimes+readInsNoHitTimes;
                totalNoHitRate=((double)totalNoHitTimes/(double)totalAccessTimes)*100;
                ip=(ip+1)%instruction_num;
            }
        }
    }

    /*
     * 模拟执行到底
     */
    public void simExecAll() {
        int i=ip;
        while(i++<instruction_num){
            simExecStep();
        }
    }
    public void allValueToMatrix(){
        resultData[0][0]=""+totalAccessTimes;
        resultData[0][1]=""+totalNoHitTimes;
        resultData[0][2]=""+totalNoHitRate;
        resultData[1][0]=""+readInsTimes;
        resultData[1][1]=""+readInsNoHitTimes;
        resultData[1][2]=""+readInsNoHitRate;
        resultData[2][0]=""+readDataTimes;
        resultData[2][1]=""+readDataNoHitTimes;
        resultData[2][2]=""+readDataNoHitRate;
        resultData[3][0]=""+writeDataTimes;
        resultData[3][1]=""+writeDataNoHitTimes;
        resultData[3][2]=""+writeDataNoHitRate;
    }

    public static void main(String[] args) {
        CCacheSim c = new CCacheSim();
        c.setResizable(false);
    }

    /**
     * 绘制 Cache 模拟器图形化界面
     * 无需做修改
     */
    public void draw() {
        //模拟器绘制面板
        setLayout(new BorderLayout(5,5));
        panelTop = new JPanel();
        panelLeft = new JPanel();
        panelRight = new JPanel();
        panelBottom = new JPanel();
        panelTop.setPreferredSize(new Dimension(800, 50));
        panelLeft.setPreferredSize(new Dimension(300, 450));
        panelRight.setPreferredSize(new Dimension(500, 450));
        panelBottom.setPreferredSize(new Dimension(800, 100));
        panelTop.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        panelLeft.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        panelRight.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        panelBottom.setBorder(new EtchedBorder(EtchedBorder.RAISED));

        //*****************************顶部面板绘制*****************************************//
        labelTop = new JLabel("Cache Simulator");
        labelTop.setAlignmentX(CENTER_ALIGNMENT);
        panelTop.add(labelTop);


        //*****************************左侧面板绘制*****************************************//
        labelLeft = new JLabel("Cache 参数设置");
        labelLeft.setPreferredSize(new Dimension(300, 40));

        uCacheButton = new JRadioButton("统一Cache的大小:",true);
        uCacheButton.setPreferredSize(new Dimension(100,30));
        uCacheButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                cacheType=0;
                uCacheButton.setSelected(true);
                ucsBox.setEnabled(true);
                sCacheButton.setSelected(false);
                dCacheLabel.setEnabled(false);
                dCacheBox.setEnabled(false);
                iCacheLabel.setEnabled(false);
                iCacheBox.setEnabled(false);
            }
        });
        ucsBox = new JComboBox(cachesize);
        ucsBox.setPreferredSize(new Dimension(160, 30));
        ucsBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED){
                    ucsIndex = ucsBox.getSelectedIndex();
                    if(change_imm){
                        initialization();
                    }
                }
            }
        });
        //独立cache
        sCacheButton=new JRadioButton("独立Cache:",false);
        sCacheButton.setPreferredSize(new Dimension(265,30));
        sCacheButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                cacheType=1;
                sCacheButton.setSelected(true);
                uCacheButton.setSelected(false);
                ucsBox.setEnabled(false);
                dCacheLabel.setEnabled(true);
                dCacheBox.setEnabled(true);
                iCacheLabel.setEnabled(true);
                iCacheBox.setEnabled(true);
            }
        });
//        dCache
        dCacheLabel=new JLabel("数据Cache的大小:");
        dCacheLabel.setPreferredSize(new Dimension(120,30));
        dCacheLabel.setEnabled(false);
        dCacheBox=new JComboBox(dcachesize);
        dCacheBox.setPreferredSize(new Dimension(90,30));
        dCacheBox.setEnabled(false);
        dCacheBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED){
                    dcsIndex = dCacheBox.getSelectedIndex();
                    if(change_imm){
                        initialization();
                    }
                }
            }
        });
//        iCache
        iCacheLabel=new JLabel("指令Cache的大小:");
        iCacheLabel.setPreferredSize(new Dimension(120,30));
        iCacheLabel.setEnabled(false);
        iCacheBox=new JComboBox(icachesize);
        iCacheBox.setPreferredSize(new Dimension(90,30));
        iCacheBox.setEnabled(false);
        iCacheBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED){
                    icsIndex = iCacheBox.getSelectedIndex();
                    if(change_imm){
                        initialization();
                    }
                }
            }
        });
        //cache 块大小设置
        bsLabel = new JLabel("块大小");
        bsLabel.setPreferredSize(new Dimension(120, 30));
        bsBox = new JComboBox(blocksize);
        bsBox.setPreferredSize(new Dimension(160, 30));
        bsBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED){
                    bsIndex = bsBox.getSelectedIndex();
                    if(change_imm){
                        initialization();
                    }
                }
            }
        });

        //相连度设置
        wayLabel = new JLabel("相联度");
        wayLabel.setPreferredSize(new Dimension(120, 30));
        wayBox = new JComboBox(way);
        wayBox.setPreferredSize(new Dimension(160, 30));
        wayBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()== ItemEvent.SELECTED){
                    wayIndex = wayBox.getSelectedIndex();
                    if(change_imm){
                        initialization();
                    }
                }
            }
        });

        //替换策略设置
        replaceLabel = new JLabel("替换策略");
        replaceLabel.setPreferredSize(new Dimension(120, 30));
        replaceBox = new JComboBox(replace);
        replaceBox.setPreferredSize(new Dimension(160, 30));
        replaceBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED){
                    replaceIndex = replaceBox.getSelectedIndex();
                    if(change_imm){
                        initialization();
                    }
                }
            }
        });

        //欲取策略设置
        prefetchLabel = new JLabel("预取策略");
        prefetchLabel.setPreferredSize(new Dimension(120, 30));
        prefetchBox = new JComboBox(pref);
        prefetchBox.setPreferredSize(new Dimension(160, 30));
        prefetchBox.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e){
                if(e.getStateChange()==ItemEvent.SELECTED){
                    prefetchIndex = prefetchBox.getSelectedIndex();
                    if(change_imm){
                        initialization();
                    }
                }
            }
        });

        //写策略设置
        writeLabel = new JLabel("写策略");
        writeLabel.setPreferredSize(new Dimension(120, 30));
        writeBox = new JComboBox(write);
        writeBox.setPreferredSize(new Dimension(160, 30));
        writeBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED){
                    writeIndex = writeBox.getSelectedIndex();
                    if(change_imm){
                        initialization();
                    }
                }
            }
        });

        //调块策略
        allocLabel = new JLabel("写不命中调块策略");
        allocLabel.setPreferredSize(new Dimension(120, 30));
        allocBox = new JComboBox(alloc);
        allocBox.setPreferredSize(new Dimension(160, 30));
        allocBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED){
                    allocIndex = allocBox.getSelectedIndex();
                    if(change_imm){
                        initialization();
                    }
                }
            }
        });

        //选择指令流文件
        fileLabel = new JLabel("选择指令流文件");
        fileLabel.setPreferredSize(new Dimension(120, 30));
        fileAddrBtn = new JLabel();
        fileAddrBtn.setPreferredSize(new Dimension(210,30));
        fileAddrBtn.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        fileBotton = new JButton("浏览");
        fileBotton.setPreferredSize(new Dimension(70,30));

        fileBotton.addActionListener(this);

        panelLeft.add(labelLeft);
        panelLeft.add(uCacheButton);
        panelLeft.add(ucsBox);
        panelLeft.add(sCacheButton);
        panelLeft.add(dCacheLabel);
        panelLeft.add(dCacheBox);
        panelLeft.add(iCacheLabel);
        panelLeft.add(iCacheBox);

        panelLeft.add(bsLabel);
        panelLeft.add(bsBox);
        panelLeft.add(wayLabel);
        panelLeft.add(wayBox);
        panelLeft.add(replaceLabel);
        panelLeft.add(replaceBox);
        panelLeft.add(prefetchLabel);
        panelLeft.add(prefetchBox);
        panelLeft.add(writeLabel);
        panelLeft.add(writeBox);
        panelLeft.add(allocLabel);
        panelLeft.add(allocBox);
        panelLeft.add(fileLabel);
        panelLeft.add(fileAddrBtn);
        panelLeft.add(fileBotton);

        //*****************************右侧面板绘制*****************************************//
        //模拟结果展示区域
        rightLabel = new JLabel("模拟结果");
        rightLabel.setPreferredSize(new Dimension(500, 40));
//        rightLabel.setHorizontalAlignment((int)CENTER_ALIGNMENT);
        panelRight.add(rightLabel);
        for (int i=0; i<4; i++) {
            for(int j=0;j<3;j++){
                if(i==1&&j==0){
                    JLabel title = new JLabel("其中:");
                    title.setPreferredSize(new Dimension(500,40));
                    panelRight.add(title);
                }
                resultLabeltag[i][j] = new JLabel(resultLabelText[i][j]);
                resultLabeltag[i][j].setPreferredSize(new Dimension(80, 40));
                resultDatatag[i][j] = new JLabel(resultData[i][j]);
                resultDatatag[i][j].setPreferredSize(new Dimension(50,40));
                panelRight.add(resultLabeltag[i][j]);
                panelRight.add(resultDatatag[i][j]);
            }
        }
        accessType = new JLabel("访问类型:");
        accessType.setPreferredSize(new Dimension(80,40));
        accessType.setVisible(false);
        panelRight.add(accessType);
        accessTypeData = new JLabel("");
        accessTypeData.setPreferredSize(new Dimension(50,40));
        accessTypeData.setVisible(false);
        panelRight.add(accessTypeData);
        address = new JLabel("地址:");
        address.setPreferredSize(new Dimension(50,40));
        address.setVisible(false);
        panelRight.add(address);
        addressData = new JLabel("");
        addressData.setPreferredSize(new Dimension(80,40));
        addressData.setVisible(false);
        panelRight.add(addressData);
        tag = new JLabel("标记tag:");
        tag.setPreferredSize(new Dimension(60,40));
        tag.setVisible(false);
        panelRight.add(tag);
        tagData = new JLabel("");
        tagData.setPreferredSize(new Dimension(80,40));
        tagData.setVisible(false);
        panelRight.add(tagData);

        blockNum = new JLabel("块号:");
        blockNum.setPreferredSize(new Dimension(80,40));
        blockNum.setVisible(false);
        panelRight.add(blockNum);
        blockNumData = new JLabel("");
        blockNumData.setPreferredSize(new Dimension(80,40));
        blockNumData.setVisible(false);
        panelRight.add(blockNumData);
        blockInAddr = new JLabel("块内地址:");
        blockInAddr.setPreferredSize(new Dimension(80,40));
        blockInAddr.setVisible(false);
        panelRight.add(blockInAddr);
        blockInAddrData = new JLabel("");
        blockInAddrData.setPreferredSize(new Dimension(50,40));
        blockInAddrData.setVisible(false);
        panelRight.add(blockInAddrData);
        index = new JLabel("索引:");
        index.setPreferredSize(new Dimension(80,40));
        index.setVisible(false);
        panelRight.add(index);
        indexData = new JLabel("");
        indexData.setPreferredSize(new Dimension(50,40));
        indexData.setVisible(false);
        panelRight.add(indexData);

        hitInfo = new JLabel("命中情况:");
        hitInfo.setPreferredSize(new Dimension(200,40));
        hitInfo.setVisible(false);
        panelRight.add(hitInfo);
        hitInfoData = new JLabel("");
        hitInfoData.setPreferredSize(new Dimension(210,40));
        hitInfoData.setVisible(false);
        panelRight.add(hitInfoData);


        //*****************************底部面板绘制*****************************************//

        bottomLabel = new JLabel("执行控制");
        bottomLabel.setPreferredSize(new Dimension(800, 30));
        execStepBtn = new JButton("步进");
        execStepBtn.setLocation(100, 30);
        execStepBtn.addActionListener(this);
        execAllBtn = new JButton("执行到底");
        execAllBtn.setLocation(300, 30);
        execAllBtn.addActionListener(this);

        panelBottom.add(bottomLabel);
        panelBottom.add(execStepBtn);
        panelBottom.add(execAllBtn);

        add("North", panelTop);
        add("West", panelLeft);
        add("Center", panelRight);
        add("South", panelBottom);
        setSize(820, 720);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
