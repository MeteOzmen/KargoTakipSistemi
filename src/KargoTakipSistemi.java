import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class KargoTakipSistemi {
    private static Scanner scanner = new Scanner(System.in);
    private static List<Customer> customers = new ArrayList<>();
    private static PriorityQueue<Shipment> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(Shipment::getDeliveryDuration));
    private static Stack<Shipment> shipmentStack = new Stack<>();

    private static JFrame frame;
    private static JPanel leftPanel, rightPanel;
    private static JTextArea shipmentHistoryArea;
    private static JTextField customerIdField, shipmentIdField, deliveryDurationField;
    private static JComboBox<String> deliveryStatusComboBox;

    public static void main(String[] args) {
        initializeGUI();
    }

    private static void initializeGUI() {
        frame = new JFrame("Kargo Yönetim Sistemi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(5, 2));

        leftPanel.add(new JLabel("Müşteri ID:"));
        customerIdField = new JTextField();
        leftPanel.add(customerIdField);

        leftPanel.add(new JLabel("Gönderi ID:"));
        shipmentIdField = new JTextField();
        leftPanel.add(shipmentIdField);

        leftPanel.add(new JLabel("Teslim Durumu:"));
        // Teslimat durumu için JComboBox oluşturma
        String[] deliveryStatusOptions = { "Teslimat Aşamasında", "Teslim Edildi", "İşleme Alındı" };
        deliveryStatusComboBox = new JComboBox<>(deliveryStatusOptions);
        leftPanel.add(deliveryStatusComboBox);

        leftPanel.add(new JLabel("Teslim Süresi (gün):"));
        deliveryDurationField = new JTextField();
        leftPanel.add(deliveryDurationField);

        JButton addButton = new JButton("Kargo Ekle");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addShipmentFromGUI();
            }
        });
        leftPanel.add(addButton);

        rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());

        shipmentHistoryArea = new JTextArea(20, 30);
        shipmentHistoryArea.setEditable(false);
        rightPanel.add(new JScrollPane(shipmentHistoryArea), BorderLayout.CENTER);

        JButton searchButton = new JButton("Kargo Sorgula");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewShipmentHistoryFromGUI();
            }
        });
        rightPanel.add(searchButton, BorderLayout.SOUTH);

        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(rightPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    private static void addShipmentFromGUI() {
        try {
            int customerId = Integer.parseInt(customerIdField.getText());
            int shipmentId = Integer.parseInt(shipmentIdField.getText());
            String deliveryStatus = (String) deliveryStatusComboBox.getSelectedItem();
            int deliveryDuration = Integer.parseInt(deliveryDurationField.getText());

            Customer customer = findCustomerById(customerId);
            if (customer == null) {
                String firstName = JOptionPane.showInputDialog(frame, "Müşteri adı:");
                String lastName = JOptionPane.showInputDialog(frame, "Müşteri soyadı:");
                customer = new Customer(customerId, firstName, lastName);
                customers.add(customer);  // Yeni müşteri listeye ekleniyor
            }

            Shipment shipment = new Shipment(shipmentId, deliveryStatus, deliveryDuration);
            customer.addShipment(shipment);
            shipmentStack.push(shipment);
            priorityQueue.add(shipment);

            JOptionPane.showMessageDialog(frame, "Kargo gönderimi eklendi.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Geçersiz sayı girdisi.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void viewShipmentHistoryFromGUI() {
        try {
            int customerId = Integer.parseInt(customerIdField.getText());
            Customer customer = findCustomerById(customerId);
            if (customer != null) {
                StringBuilder history = new StringBuilder();
                history.append("Müşteri: ").append(customer.getFirstName()).append(" ").append(customer.getLastName()).append("\n\n");

                List<Shipment> sortedShipments = new ArrayList<>(customer.getShipmentHistory());
                sortedShipments.sort(Comparator.comparingInt(Shipment::getDeliveryDuration));

                for (Shipment shipment : sortedShipments) {
                    history.append("Gönderi ID: ").append(shipment.getShipmentId())
                            .append(", Durum: ").append(shipment.getDeliveryStatus())
                            .append(", Teslim Süresi: ").append(shipment.getDeliveryDuration()).append(" gün\n");
                }
                shipmentHistoryArea.setText(history.toString());
            } else {
                JOptionPane.showMessageDialog(frame, "Müşteri bulunamadı.", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Geçersiz Müşteri ID.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Customer findCustomerById(int id) {
        for (Customer customer : customers) {
            if (customer.getCustomerId() == id) {
                return customer;
            }
        }
        return null;
    }

    private static void checkShipmentStatus() {
        System.out.print("Gönderi ID: ");
        int shipmentId = scanner.nextInt();
        Shipment shipment = findShipmentById(shipmentId);
        if (shipment != null) {
            System.out.println("Gönderi Durumu: " + shipment.getDeliveryStatus());
        } else {
            System.out.println("Gönderi bulunamadı.");
        }
    }

    private static Shipment findShipmentById(int shipmentId) {
        for (Customer customer : customers) {
            for (Shipment shipment : customer.getShipmentHistory()) {
                if (shipment.getShipmentId() == shipmentId) {
                    return shipment;
                }
            }
        }
        return null;
    }

    private static void viewShipmentHistory() {
        System.out.print("Müşteri ID: ");
        int customerId = scanner.nextInt();
        Customer customer = findCustomerById(customerId);
        if (customer != null) {
            customer.displayShipmentHistory();
        } else {
            System.out.println("Müşteri bulunamadı.");
        }
    }

    private static void listAllShipments() {
        System.out.println("Kargo Listele:");
        Shipment[] shipments = shipmentStack.toArray(new Shipment[0]);
        Arrays.sort(shipments, Comparator.comparingInt(Shipment::getDeliveryDuration));
        for (Shipment shipment : shipments) {
            System.out.println("Gönderi ID: " + shipment.getShipmentId() + ", Durum: " + shipment.getDeliveryStatus() + ", Teslim Süresi: " + shipment.getDeliveryDuration() + " gün");
        }
    }

    static class Shipment {
        private int shipmentId;
        private String deliveryStatus;
        private int deliveryDuration;

        public Shipment(int shipmentId, String deliveryStatus, int deliveryDuration) {
            this.shipmentId = shipmentId;
            this.deliveryStatus = deliveryStatus;
            this.deliveryDuration = deliveryDuration;
        }

        public int getShipmentId() {
            return shipmentId;
        }

        public String getDeliveryStatus() {
            return deliveryStatus;
        }

        public int getDeliveryDuration() {
            return deliveryDuration;
        }
    }

    static class Customer {
        private int customerId;
        private String firstName;
        private String lastName;
        private LinkedList<Shipment> shipmentHistory;

        public Customer(int customerId, String firstName, String lastName) {
            this.customerId = customerId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.shipmentHistory = new LinkedList<>();
        }

        public int getCustomerId() {
            return customerId;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public LinkedList<Shipment> getShipmentHistory() {
            return shipmentHistory;
        }

        public void addShipment(Shipment shipment) {
            int i = 0;
            while (i < shipmentHistory.size() && shipmentHistory.get(i).getDeliveryDuration() < shipment.getDeliveryDuration()) {
                i++;
            }
            shipmentHistory.add(i, shipment);
        }

        public void displayShipmentHistory() {
            for (Shipment shipment : shipmentHistory) {
                System.out.println("Shipment ID: " + shipment.getShipmentId() + ", Status: " + shipment.getDeliveryStatus() + ", Duration: " + shipment.getDeliveryDuration() + " days");
            }
        }
    }
}
