/*******************************************************************************************************
Copyright (c) 2011 Regents of the University of California.
All rights reserved.

This software was developed at the University of California, Irvine.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.

3. All advertising materials mentioning features or use of this
   software must display the following acknowledgment:
   "This product includes software developed at the University of
   California, Irvine by Nicolas Oros, Ph.D.
   (http://www.cogsci.uci.edu/~noros/)."

4. The name of the University may not be used to endorse or promote
   products derived from this software without specific prior written
   permission.

5. Redistributions of any form whatsoever must retain the following
   acknowledgment:
   "This product includes software developed at the University of
   California, Irvine by Nicolas Oros, Ph.D.
   (http://www.cogsci.uci.edu/~noros/)."

THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
IN NO EVENT SHALL THE UNIVERSITY OR THE PROGRAM CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*******************************************************************************************************/

package com.eMagic.robot;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IOIO_Thread_UDP implements Runnable
{
    float gas;
    CAR_GUI car_state;
    Thread t;
    int size_p;

    public IOIO_Thread_UDP(CAR_GUI gui) 
    {
        car_state = gui;
        try 
        {
        	t = new Thread(this);
        	t.start();
        } 
        catch (Exception e){e.printStackTrace();}
    }
    
    @Override
    public void run() 
    {
    	car_state.addLogMessage("Esperando IP de movil ...");
        handleConnection_UDP2();
    }
    // Version con reles
    public void handleConnection_UDP2() {
        
        DatagramSocket socket=null;
        byte data = 0, dataOld=127;
        byte dataAux[] = new byte[1]; 
        byte[] buffer;
        DatagramPacket packet_R=null;
        InetAddress IP_phone, serverAddr=null;
        int port_phone;
        
        try {
            serverAddr = InetAddress.getLocalHost();
            socket = new DatagramSocket(6791); //, serverAddr);        
            buffer = new byte[InetAddress.getLocalHost().getAddress().length];  // Array para que quepa IP
            packet_R = new DatagramPacket(buffer, buffer.length);            
            socket.receive(packet_R); // Recibimos dirección IP y puerto del movil (cliente)
        } catch (IOException e) {
            Logger.getLogger(IOIO_Thread_UDP.class.getName()).log(Level.SEVERE, null, e);
        }   
        
	IP_phone = packet_R.getAddress();        
	port_phone = packet_R.getPort();        
	car_state.addLogMessage("Recibida IP " + IP_phone + ":" + port_phone + " del móvil");
        car_state.setIOIOIPPortClient(serverAddr.getHostAddress(), "6791", IP_phone.getHostAddress(), port_phone+"");
                
    	while (true) { 	
            // Izquierda, Derecha, Adelante, Atras, Luz y Bocina
            if (car_state.RIGHT) 
                data = (byte)(data | 80); // pin 7 y 5 activado
            else  if (car_state.LEFT) // Giro a la izquierda 
                data = (byte)(data | 40); // pin 6 y 5 activado
            else if (car_state.UP) 
                data = (byte)(data | 72); // Pin 4 y 7
            else if (car_state.DOWN) 
                data = (byte)(data | 48); // Pin 5 y 6
            else if (car_state.UPLEFT)
                data = (byte)(data | 64); // Sin referencia
            else if (car_state.UPRIGHT)
                data = (byte)(data | 32); // Sin referencia
            else if (car_state.DOWNLEFT)
                data = (byte)(data | 16); // Sin referencia
            else if (car_state.DOWNRIGHT)
                data = (byte)(data | 8); // Sin referencia
            else 
                data = (byte)(data & 3); // Dirección desactivada manteniendo activa luz y bocina si estuviera
            
            // Activación Luz y Bocina
            if (car_state.LUZ) 
                data = (byte)(data | 1); // Pin 1
            else
                data = (byte)(data & 126); // Desactivar Luz
            if (car_state.BOCINA) 
                data = (byte)(data | 2); // Pin 2
            else
                data = (byte)(data & 125); // Desactivar Bocina
                        
            // Para bucle del servidor
            if (!car_state.SEGUIR_BUCLE)
                data = (byte)127;
            
            if(port_phone > -1)   {    
                if (dataOld != data) {
                    try {
                        car_state.addLogMessage("Datos Navegación: " + data );
                        dataOld = data;
                        dataAux[0] = data;
                        DatagramPacket packet_S = new DatagramPacket(dataAux, 1, IP_phone, port_phone);
                        socket.send(packet_S);
                        
                    } catch (IOException ex) {
                        Logger.getLogger(IOIO_Thread_UDP.class.getName()).log(Level.SEVERE, null, ex);
                    }                
                }           
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(IOIO_Thread_UDP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }  
    }
}
