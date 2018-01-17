import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;
import java.util.jar.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;

public class Mandelbrot extends Applet {
    private Graphics graphics1;
    private boolean julia;
    private int nx,
                ny,
                imax=50,
                method=0,
                fcolor=0;
    private double x0=-0.5,
                   y0=0,
                   dpi,
                   power=2,
                   xc=0,
                   yc=0;
    private TextField tf1,tf2,tf3,tf4,tf5,tf6,tf7,tf8;
    private Choice choice1=new Choice(),
                   choice2=new Choice(),
                   choice3=new Choice(),
                   choice4=new Choice(),
                   choice5=new Choice();
    private Button button1=new Button("Draw"),
                   button2=new Button("Stop");
    private imagePanel FractalField;
    private int mouseMode=0;
    private int textMode=0;
    public TimerTask tt;
    public int c1,c2,r,g,b, i, n;
    public double x, y, dx, x1, x2, y1, y2;
    public double t1, t2, zm;
    public int ix, iy;
    public boolean unbounded;
    java.util.Timer timer=new java.util.Timer();
    public double dzoom;
    private boolean done=false;
    
    public TextField editbox(String str, double value, int x, int y, int w) { // use template, overloading
        Label label=new Label(str+":");
        label.setBounds(x,y,w,25);
        add(label);
        TextField tf=new TextField(20);
        tf.setBounds(x+w,y,102-w,25);
        tf.setText(String.valueOf(value));
        add(tf);
        return tf;
    }
    public void init() {
        setLayout(null);
        nx=size().width-20; ny=size().height-160; dpi=nx/3;
        setSize(nx+20,ny+160); // just in case so that the size is fixed
        FractalField=new imagePanel(nx,ny);
        choice1.addItem("Mandelbrot set");
        choice1.addItem("Julia set");
        choice1.select(0);
        choice1.setBounds(10,ny+20,122,25);
        add(choice1);
        tf4=editbox("Xc",xc,142,ny+20,20);
        tf5=editbox("Yc",yc,248,ny+20,20);
        tf6=editbox("X0",x0,354,ny+20,20);
        tf7=editbox("Y0",y0,460,ny+20,20);
        tf1=editbox("Size",dpi,248,ny+55,52);
        tf8=editbox("Zoom in",10,142,ny+55,52);
        tf2=editbox("Iter.",0,364,ny+55,42);
        tf2.requestFocus();
        tf2.setText(String.valueOf(imax));
        tf3=editbox("Degree",power,476,ny+55,55);
        choice2.addItem("Iterations");
        choice2.addItem("Modulus");
        choice2.addItem("Real part");
        choice2.addItem("Imaginary part");
        choice2.setBounds(10,ny+55,102,25);
        add(choice2);
        choice3.addItem("Red");
        choice3.addItem("Green");
        choice3.addItem("Blue");
        choice3.addItem("Yellow");
        choice3.addItem("Cyan");
        choice3.addItem("Magenta");
        choice3.addItem("White");
        choice3.addItem("Multi-color");
        choice3.setBounds(10,ny+90,102,25);
        add(choice3);
        choice4.addItem("Mouse: x0/y0. Zoom.");
        choice4.addItem("Mouse: x0/y0. Do not zoom.");
        choice4.addItem("Mouse: xc/yc");
        choice4.addItem("Mouse: iterations++");
        choice4.setBounds(142,ny+90,142,25);
        add(choice4);
        choice5.addItem("With black label");
        choice5.addItem("With white label");
        choice5.addItem("No labels");
        choice5.setBounds(294,ny+90,112,25);
        add(choice5);
        button1.setBounds(167,ny+125,102,25);
        button2.setBounds(289,ny+125,102,25);
        FractalField.setBounds(0,0,nx+20,ny+10);
        add(button1);
        add(button2);
        add(FractalField);
        FractalField.Buffer=new BufferedImage(nx+20, ny+10, BufferedImage.TYPE_INT_RGB);
        graphics1=FractalField.Buffer.getGraphics();
        graphics1.setColor(Color.white);
        graphics1.fillRect(0,0,nx+20,ny+10);
        FractalField.getGraphics().drawImage(FractalField.Buffer,0,0,nx+20,ny+10, FractalField);
    }

    // Input
    public boolean action(Event event1, Object object1) {
        if(event1.target==button1) {
            done=true;
            String str;
            str=tf1.getText(); if(str!=null&&str.length()!=0) dpi=Double.valueOf(str).doubleValue();
            str=tf2.getText(); if(str!=null&&str.length()!=0) imax=Integer.parseInt(str);
            str=tf3.getText(); if(str!=null&&str.length()!=0) power=Double.valueOf(str).doubleValue();
            str=tf4.getText(); if(str!=null&&str.length()!=0) xc=Double.valueOf(str).doubleValue();
            str=tf5.getText(); if(str!=null&&str.length()!=0) yc=Double.valueOf(str).doubleValue();
            str=tf6.getText(); if(str!=null&&str.length()!=0) x0=Double.valueOf(str).doubleValue();
            str=tf7.getText(); if(str!=null&&str.length()!=0) y0=Double.valueOf(str).doubleValue();
            generate();
        } else if(event1.target==button2) {
            tt.cancel();
        } else if(event1.target==choice1) {
            julia=(choice1.getSelectedIndex()==1);
            if(julia) tf6.setText(String.valueOf(0.0));
            else {
                tf4.setText(String.valueOf(0.0));
                tf5.setText(String.valueOf(0.0));
                tf6.setText(String.valueOf(-0.5));
            }
            tf7.setText(String.valueOf(0.0));
            tf1.setText(String.valueOf(nx/3));
        }
        else if(event1.target==choice2) method=choice2.getSelectedIndex();
        else if(event1.target==choice3) fcolor=choice3.getSelectedIndex();
        else if(event1.target==choice4) mouseMode=choice4.getSelectedIndex();
        else if(event1.target==choice5) textMode=choice5.getSelectedIndex();
        return true;
    }
    public boolean mouseDown(Event event1, int x, int y) {
        if(((x>=0)&&(x<=nx+20))&&(done)&&((y>=0)&&(y<=ny+10))) {
            x-=10; y-=10;
            if(mouseMode==2) {
                tf4.setText(String.valueOf(x0+(x-0.5*nx)/dpi));
                tf5.setText(String.valueOf(y0-(y-0.5*ny)/dpi));
                choice1.select(1);
                choice4.select(0);
                mouseMode=0;
                julia=true;
                tf6.setText(String.valueOf(0.0));
                tf7.setText(String.valueOf(0.0));
                tf1.setText(String.valueOf(nx/3));
                String str;
                str=tf8.getText(); if(str!=null&&str.length()!=0) dzoom=Double.valueOf(str).doubleValue();
                str=tf1.getText(); if(str!=null&&str.length()!=0) dpi=Double.valueOf(str).doubleValue();
                str=tf2.getText(); if(str!=null&&str.length()!=0) imax=Integer.parseInt(str);
                str=tf3.getText(); if(str!=null&&str.length()!=0) power=Double.valueOf(str).doubleValue();
                str=tf4.getText(); if(str!=null&&str.length()!=0) xc=Double.valueOf(str).doubleValue();
                str=tf5.getText(); if(str!=null&&str.length()!=0) yc=Double.valueOf(str).doubleValue();
                str=tf6.getText(); if(str!=null&&str.length()!=0) x0=Double.valueOf(str).doubleValue();
                str=tf7.getText(); if(str!=null&&str.length()!=0) y0=Double.valueOf(str).doubleValue();
                generate();
            } else if(mouseMode==0) {
                tf6.setText(String.valueOf(x0+(x-0.5*nx)/dpi));
                tf7.setText(String.valueOf(y0-(y-0.5*ny)/dpi));
                String str;
                str=tf8.getText(); if(str!=null&&str.length()!=0) dzoom=Double.valueOf(str).doubleValue();
                tf1.setText(String.valueOf(dzoom*dpi));
                str=tf1.getText(); if(str!=null&&str.length()!=0) dpi=Double.valueOf(str).doubleValue();
                str=tf2.getText(); if(str!=null&&str.length()!=0) imax=Integer.parseInt(str);
                str=tf3.getText(); if(str!=null&&str.length()!=0) power=Double.valueOf(str).doubleValue();
                str=tf4.getText(); if(str!=null&&str.length()!=0) xc=Double.valueOf(str).doubleValue();
                str=tf5.getText(); if(str!=null&&str.length()!=0) yc=Double.valueOf(str).doubleValue();
                str=tf6.getText(); if(str!=null&&str.length()!=0) x0=Double.valueOf(str).doubleValue();
                str=tf7.getText(); if(str!=null&&str.length()!=0) y0=Double.valueOf(str).doubleValue();
                generate();
            } else if(mouseMode==1) {
                tf6.setText(String.valueOf(x0+(x-0.5*nx)/dpi));
                tf7.setText(String.valueOf(y0-(y-0.5*ny)/dpi));
                String str;
                str=tf8.getText(); if(str!=null&&str.length()!=0) dzoom=Double.valueOf(str).doubleValue();
                str=tf1.getText(); if(str!=null&&str.length()!=0) dpi=Double.valueOf(str).doubleValue();
                str=tf2.getText(); if(str!=null&&str.length()!=0) imax=Integer.parseInt(str);
                str=tf3.getText(); if(str!=null&&str.length()!=0) power=Double.valueOf(str).doubleValue();
                str=tf4.getText(); if(str!=null&&str.length()!=0) xc=Double.valueOf(str).doubleValue();
                str=tf5.getText(); if(str!=null&&str.length()!=0) yc=Double.valueOf(str).doubleValue();
                str=tf6.getText(); if(str!=null&&str.length()!=0) x0=Double.valueOf(str).doubleValue();
                str=tf7.getText(); if(str!=null&&str.length()!=0) y0=Double.valueOf(str).doubleValue();
                generate();
            } else if(mouseMode==3) {
                String str;
                str=tf2.getText(); if(str!=null&&str.length()!=0) imax=Integer.parseInt(str);
                tf2.setText(String.valueOf(imax+1));
                imax+=1;
                str=tf8.getText(); if(str!=null&&str.length()!=0) dzoom=Double.valueOf(str).doubleValue();
                str=tf1.getText(); if(str!=null&&str.length()!=0) dpi=Double.valueOf(str).doubleValue();
                str=tf3.getText(); if(str!=null&&str.length()!=0) power=Double.valueOf(str).doubleValue();
                str=tf4.getText(); if(str!=null&&str.length()!=0) xc=Double.valueOf(str).doubleValue();
                str=tf5.getText(); if(str!=null&&str.length()!=0) yc=Double.valueOf(str).doubleValue();
                str=tf6.getText(); if(str!=null&&str.length()!=0) x0=Double.valueOf(str).doubleValue();
                str=tf7.getText(); if(str!=null&&str.length()!=0) y0=Double.valueOf(str).doubleValue();
                generate();
            }
            graphics1=FractalField.Buffer.getGraphics();
            pixel(x,y,255,255,255);
            FractalField.getGraphics().drawImage(FractalField.Buffer,0,0,nx+20,ny+10, FractalField);
        }
        return true;
    }

    // Calculation
    public double sinh(double x) {double t1=Math.exp(x); return (t1-1.0/t1)/2.0;}
    public double cosh(double x) {double t1=Math.exp(x); return (t1+1.0/t1)/2.0;}
    private double letsRound(double d, int i) {
        int j=i;
        BigDecimal bigdecimal=new BigDecimal(d);
        bigdecimal=bigdecimal.setScale(j,bigdecimal.ROUND_HALF_UP);
        d=bigdecimal.doubleValue();
        return d;
    }

	// Generate figure
	public void pixel(int x, int y, int r, int g, int b) {
		Color color1=new Color(r,g,b);
		graphics1.setColor(color1);
		graphics1.fillRect(x+10,y+5,1,1);
	}
	public double izm=0;
	public void generate() {
		graphics1=FractalField.Buffer.getGraphics();
		graphics1.setColor(Color.white);
		graphics1.fillRect(0,0,nx+20,ny+10);
		r=0;
        g=0;
        b=0;
        n=(int)power/2-((power%2!=0)?1:0);
		x=0;
        y=0;
        dx=1/dpi;
        x1=x0-0.5*nx/dpi;
        x2=x0+0.5*nx/dpi;
        y1=y0-ny*dx/2;
        y2=y0+ny*dx/2;
		t1=Math.max(Math.abs(x1),Math.abs(x2));
        t2=Math.max(Math.abs(y1),Math.abs(y2));
        zm=Math.sqrt(t1*t1+t2*t2);
		double ix1=-0.5-0.5*nx/dpi;
		double ix2=-0.5+0.5*nx/dpi;
		double iy1=ny*dx/2;
		double it1=Math.max(Math.abs(ix1),Math.abs(ix2));
		double it2=Math.abs(iy1);
		izm=Math.sqrt(it1*it1+it2*it2);
		ix=0;
		iy=-1;
		tt= new TimerTask() {
            public void run() {
                for(int jjrun=0;jjrun<=1000;jjrun++) {
                    if(ix<=nx) {
                        ix++;
                        if(julia) {
                            x=x1+ix*dx;
                            y=y2-iy*dx;
                        } else {
                            xc=x1+ix*dx;
                            x=0;
                            y=0;
                        }
                        unbounded=false;
                        for (i=0; (i<imax)&&(!unbounded); ++i) {
                            if(power>=2&&power<=25&&power%1==0) {
                                for(int j=0;j<n;++j) {
                                    t1=x*x-y*y;
                                    y=2*x*y;
                                    x=t1;
                                }
                                if(power%2!=0) {
                                    t1=x*(x*x-3*y*y);
                                    y=y*(3*x*x-y*y);
                                    x=t1;
                                }
                            } else {
                                t1=Math.atan2(y,x);
                                t2=Math.pow(x*x+y*y,power/2.0);
                                x=t2*Math.cos(power*t1);
                                y=t2*Math.sin(power*t1);
                            }
                            x+=xc; y+=yc;
                            if(x*x+y*y>=4.0) unbounded=true;
                        }
                        if(unbounded) {
                            if(fcolor!=7) {
                                if(method==0) t1=1.0*i/imax; // based on iteration
                                else if(method==1) t1=(Math.sqrt(x*x+y*y)>=2)?0:1; // based on modulus
                                else t1=Math.min(1,Math.abs((method==2)?x:y)/(double)izm); // based on imag or real parts
                                if(fcolor==6) {r=255-(int)(255*t1); g=r; b=r;}// black
                                else {
                                    c1=(int)Math.min(255*t1,255);
                                    c2=(int)Math.max(255*Math.abs(t1-1),0);
                                    switch(fcolor) {
                                        case 4: r=255-c2; g=255-c1; b=255-c1; break; // cyan
                                        case 5: r=255-c1; g=255-c2; b=255-c1; break; // magenta
                                        case 3: r=255-c1; g=255-c1; b=255-c2; break; // yellow
                                        case 2: r=255-c2; g=255-c2; b=255-c1; break; // blue
                                        case 0: r=255-c1; g=255-c2; b=255-c2; break; // red
                                        case 1: r=255-c2; g=255-c1; b=255-c2; break; // green       
                                    }
                                }
                            } else {
                                if(method==0) t1=i-((int)((double)i/(double)8))*8; // based on iteration
                                else if(method==1) t1=(Math.sqrt(x*x+y*y)>=2)?0:7; // based on modulus
                                else t1=7*Math.min(1,Math.abs((method==2)?x:y)/(double)izm); // based on imag or real parts
                                t1=Math.min(7,t1);
                                int tmp1=(int)t1;
                                switch(tmp1) {
                                    case 0: r=255;g=255;b=255;break;
                                    case 7: r=0;g=0;b=0;break;
                                    case 1: r=255;g=255;b=0;break;
                                    case 2: r=0;g=255;b=255;break;
                                    case 3: r=255;g=0;b=255;break;
                                    case 4: r=0;g=255;b=0;break;
                                    case 5: r=255;g=0;b=0;break;
                                    case 6: r=0;g=0;b=255;break;
                                }
                            }
                            pixel(ix,iy,r,g,b);
                        } else if(method==1) {
                            pixel(ix,iy,0,0,0);
                        }
                    } 
                    if(ix>nx) {
                        FractalField.getGraphics().drawImage(FractalField.Buffer,0,0,nx+20,ny+10, FractalField);
                        iy++;
                        ix=0;
                        if(iy>ny) {
                            tt.cancel();
                            if(textMode!=2) {
                                // draw labels
                                if(textMode==1) graphics1.setColor(Color.white);
                                else graphics1.setColor(Color.black);
                                graphics1.setFont(new Font("Verdana", Font.BOLD, 12));
                                int sc=9, sc2=9*5/3, ytxt=ny+10-5+sc2;
                                //information about coordinates and scale
                                String str="";
                                int ddpi=nx/3;
                                str+="Zoom: "+letsRound(dpi/ddpi,2)+"x, center: "+(x0);
                                if(y0>=0) str+="+"+(y0)+"i";
                                else str+=""+(y0)+"i";
                                graphics1.drawString(str, 15, ytxt-=sc2);
                                str="";
                                //information about iterations
                                graphics1.drawString("Max iterations: "+imax, 15, ytxt-=sc2);
                                //information about set
                                if(julia) {
                                    str="Julia set: z^";
                                    if(power>=0) str+=""+letsRound(power,2);
                                    else str+="("+letsRound(power,2)+")";
                                    if(xc>=0) str+="+"+(xc);
                                    else str+=""+(xc);
                                    if(yc>=0) str+="+"+(yc)+"i";
                                    else str+=""+(yc)+"i";
                                    graphics1.drawString(str, 15, ytxt-=sc2);
                                } else {
                                    str="Mandelbrot set: z^";
                                    if(power>=0) str+=""+letsRound(power,2)+"+zc";
                                    else str+="("+letsRound(power,2)+")"+"+zc";
                                    graphics1.drawString(str, 15, ytxt-=sc2);
                                }
                            }
                            FractalField.getGraphics().drawImage(FractalField.Buffer,0,0,nx+20,ny+10, FractalField);
                        }
                        if(!julia) {
                            yc=y2-iy*dx;
                            if(iy==0) {
                                graphics1.setColor(Color.white);
                                graphics1.fillRect(0,0,nx+20,5);
                            }
                        }
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(tt,0,1);
    }
}

class imagePanel extends Panel {
    public BufferedImage Buffer;
    private int nx=10;
    private int ny=10;
    public imagePanel(int x, int y) {
        super();
        nx=x;
        ny=y;
    }
    public void paint(Graphics g) {
        g.drawImage(Buffer,0,0,nx+20,ny+10, this);
    }
    public void update(Graphics g) {
        g.drawImage(Buffer,0,0,nx+20,ny+10, this);
    }
    public void repaint(Graphics g) {
        g.drawImage(Buffer,0,0,nx+20,ny+10, this);
    }
}
