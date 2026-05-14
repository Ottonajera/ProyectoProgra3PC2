/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agencia.cliente.registro;

/**
 *
 * @author OTTO NAJERA
 */
public class PrincipalRegistro {
    public static void main(String[] args) {
        agencia.configuracion.TemaAgencia.aplicar();
        
        System.out.println("--- Iniciando secuencia de arranque del Servidor ---");
        java.awt.EventQueue.invokeLater(() -> {
            new InterfazRegistro().setVisible(true);
        });
        
    }
}
