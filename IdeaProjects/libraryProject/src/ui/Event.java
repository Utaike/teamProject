package ui;

import javax.swing.*;

public class Event{
    int n;
    public Event(int a){
        n=a;
    }
    public void print(){
        System.out.println(n);
    }

    public static void main(String[] args) {
        Event event =new Event(55);
        event.print();
    }
}
