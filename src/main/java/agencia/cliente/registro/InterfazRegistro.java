    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agencia.cliente.registro;

/**
 *
 * @author OTTO NAJERA
 */
import agencia.cliente.chat.VentanaChat;
import agencia.configuracion.ConfiguracionRed;
import agencia.modelos.TicketViaje;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class InterfazRegistro extends JFrame {

    private JTextField txtDpi;
    private JComboBox<String> cmbTipoAtencion;
    private JButton btnRegistrar, btnChat, btnBuscarHistorial; 
    private int contGeneral = 1, contPrioritario = 1, contEspecial = 1;
    private Socket socketPersistente;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public InterfazRegistro() {
        setTitle("PC2 - Registro de Tickets");
        setSize(570, 340); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setLayout(new GridLayout(4, 1, 10, 10));

        JPanel panelDpi = new JPanel();
        panelDpi.add(new JLabel("Número de DPI:"));
        txtDpi = new JTextField(15);
        txtDpi.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) || txtDpi.getText().length() >= 13) {
                    evt.consume();
                }
            }
        });
        panelDpi.add(txtDpi);
        
        JPanel panelTipo = new JPanel();
        panelTipo.add(new JLabel("Tipo de Viaje/Atención:"));
        cmbTipoAtencion = new JComboBox<>(new String[]{"GENERAL", "PRIORITARIO", "ESPECIAL"});
        panelTipo.add(cmbTipoAtencion);
        
        JPanel panelBoton = new JPanel();
        
        btnRegistrar = new JButton("Generar Ticket y Encolar");
        btnRegistrar.addActionListener((ActionEvent e) -> registrarPasajero());
        panelBoton.add(btnRegistrar);
        
        btnBuscarHistorial = new JButton("Buscar Historial");
        btnBuscarHistorial.addActionListener((ActionEvent e) -> buscarHistorialPorDPI());
        panelBoton.add(btnBuscarHistorial);
        
        btnChat = new JButton("Abrir Chat");
        btnChat.addActionListener((ActionEvent e) -> {
            new VentanaChat("Otto Najera").setVisible(true);
        });
        panelBoton.add(btnChat);

        add(new JLabel("   Bienvenido a Viajes Globales", SwingConstants.CENTER));
        add(panelDpi);
        add(panelTipo);
        add(panelBoton);
        
        conectarAlServidor();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                desconectar();
            }
        });
    }

    private void conectarAlServidor() {
        ConfiguracionRed config = new ConfiguracionRed();
        try {
            socketPersistente = new Socket(config.getIpServidor(), config.getPuerto());
            out = new ObjectOutputStream(socketPersistente.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socketPersistente.getInputStream());
            System.out.println("Conexión persistente establecida con el servidor.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "No se pudo conectar al servidor. Asegúrese de que esté encendido.", 
                "Error de Red", JOptionPane.ERROR_MESSAGE);
            btnRegistrar.setEnabled(false); 
            btnBuscarHistorial.setEnabled(false); 
        }
    }

    private void registrarPasajero() {
        if (socketPersistente == null || socketPersistente.isClosed()) {
            JOptionPane.showMessageDialog(this, "No hay conexión con el servidor.");
            return;
        }

        String dpi = txtDpi.getText().trim();
        String tipo = (String) cmbTipoAtencion.getSelectedItem();

        if (dpi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese el DPI.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String numeroTicket = "";
        int prioridad = 1; 

        switch (tipo) {
            case "GENERAL": numeroTicket = "G-" + String.format("%03d", contGeneral++); break;
            case "PRIORITARIO": numeroTicket = "P-" + String.format("%03d", contPrioritario++); prioridad = 10; break;
            case "ESPECIAL": numeroTicket = "E-" + String.format("%03d", contEspecial++); break;
        }

        TicketViaje nuevoTicket = new TicketViaje(numeroTicket, dpi, tipo, prioridad);
        enviarAlServidor(nuevoTicket);
    }

    private void enviarAlServidor(TicketViaje ticket) {
        try {
            out.writeObject("REGISTRAR"); 
            out.writeObject(ticket);
            out.flush();

            String respuesta = (String) in.readObject();
            JOptionPane.showMessageDialog(this, respuesta, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            txtDpi.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error enviando datos al servidor.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarHistorialPorDPI() {

    String dpiBusqueda = JOptionPane.showInputDialog(this, "Ingrese el DPI del pasajero a consultar:");
    if (dpiBusqueda == null || dpiBusqueda.trim().isEmpty()) {
        return;
    }
    
    dpiBusqueda = dpiBusqueda.trim();

    try {
        out.writeObject("BUSCAR_HISTORIAL_DPI");
        out.writeObject(dpiBusqueda);
        out.flush();
        Object respuesta = in.readObject();

        if (respuesta instanceof String) {
            String texto = (String) respuesta;

            if (texto.contains("No se encontró")) {
                JOptionPane.showMessageDialog(this, texto, "Sin resultados", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JTextArea textArea = new JTextArea();
            textArea.setText(texto);
            textArea.setEditable(false);
            textArea.setFont(new Font("Consolas", Font.BOLD, 15));
            textArea.setBackground(new Color(20, 20, 20));
            textArea.setForeground(new Color(50, 255, 50));
            textArea.setMargin(new Insets(15, 15, 15, 15));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(750, 450)); 

            JOptionPane.showMessageDialog(this, scrollPane, "HISTORIAL COMPLETO - DPI: " + dpiBusqueda, JOptionPane.PLAIN_MESSAGE);
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error de comunicacion: " + e.getMessage());
        e.printStackTrace();
    }
}

    private void desconectar() {
        try {
            if (socketPersistente != null && !socketPersistente.isClosed()) {
                if(out!=null){
                    out.writeObject("DESCONEXION_REAL");
                    out.flush();
                }
                socketPersistente.close();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}