package com.uam.tiendajavafx.controller;

import com.uam.tiendajavafx.dao.CategoriaDAO;
import com.uam.tiendajavafx.dao.ProductoDAO;
import com.uam.tiendajavafx.model.Categoria;
import com.uam.tiendajavafx.model.Producto;
import com.uam.tiendajavafx.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.sql.SQLException;

public class MainController {
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    @FXML private Label lblEstado;

    // Categorias
    @FXML private TextField txtCategoria;
    @FXML private CheckBox chkCategoriaActiva;
    @FXML private TableView<Categoria> tablaCategorias;
    @FXML private TableColumn<Categoria, Integer> colCategoriaId;
    @FXML private TableColumn<Categoria, String> colCategoriaNombre;
    @FXML private TableColumn<Categoria, Boolean> colCategoriaActiva;

    // Productos
    @FXML private TextField txtCodigo;
    @FXML private TextField txtProducto;
    @FXML private ComboBox<Categoria> cbCategoria;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtExistencia;
    @FXML private TextField txtRutaImagen;
    @FXML private CheckBox chkActivo;
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, Integer> colProductoId;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colProductoNombre;
    @FXML private TableColumn<Producto, String> colProductoCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, Boolean> colActivo;

    @FXML
    public void initialize() {
        configurarTablas();
        cargarCategorias();
        cargarProductos();
        probarConexion();
        tablaCategorias.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> cargarCategoria(newV));
        tablaProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> cargarProducto(newV));
    }

    private void configurarTablas() {
        colCategoriaId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getId()));
        colCategoriaNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));
        colCategoriaActiva.setCellValueFactory(data -> new javafx.beans.property.SimpleBooleanProperty(data.getValue().isActiva()).asObject());

        colProductoId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getId()));
        colCodigo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCodigo()));
        colProductoNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));
        colProductoCategoria.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCategoria().getNombre()));
        colPrecio.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrecioVenta()));
        colExistencia.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getExistencia()));
        colActivo.setCellValueFactory(data -> new javafx.beans.property.SimpleBooleanProperty(data.getValue().isActivo()).asObject());
    }

    @FXML private void probarConexion() {
        if (DatabaseConnection.testConnection()) {
            lblEstado.setText("Conexión exitosa con PostgreSQL");
        } else {
            lblEstado.setText("No se pudo conectar con PostgreSQL");
        }
    }

    @FXML private void guardarCategoria() {
        String nombre = txtCategoria.getText().trim();
        if (nombre.isBlank()) {
            alert(Alert.AlertType.WARNING, "Dato requerido", "Escribe el nombre de la categoría.");
            return;
        }

        boolean existe = tablaCategorias.getItems().stream()
                .anyMatch(c -> c.getNombre().equalsIgnoreCase(nombre));

        if (existe) {
            alert(Alert.AlertType.WARNING, "Categoría duplicada", "Ya existe una categoría con este nombre.");
            return;
        }

        try {
            categoriaDAO.guardar(new Categoria(null, nombre, chkCategoriaActiva.isSelected()));
            limpiarCategoria();
            cargarCategorias();
            cargarProductos();
            info("Categoría guardada", "La categoría se registró correctamente.");
        } catch (SQLException e) { error(e); }
    }

    @FXML private void actualizarCategoria() {
        Categoria seleccionada = tablaCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            alert(Alert.AlertType.WARNING, "Selecciona una categoría", "Selecciona una fila para actualizar.");
            return;
        }
        try {
            seleccionada.setNombre(txtCategoria.getText().trim());
            seleccionada.setActiva(chkCategoriaActiva.isSelected());
            categoriaDAO.actualizar(seleccionada);
            cargarCategorias();
            cargarProductos();
            info("Categoría actualizada", "Los cambios fueron guardados.");
        } catch (SQLException e) { error(e); }
    }

    @FXML private void eliminarCategoria() {
        Categoria seleccionada = tablaCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) { alert(Alert.AlertType.WARNING, "Selecciona una categoría", "Selecciona una fila."); return; }
        try {
            categoriaDAO.eliminar(seleccionada.getId());
            limpiarCategoria();
            cargarCategorias();
        } catch (SQLException e) {
            alert(Alert.AlertType.ERROR, "No se puede eliminar", "La categoría puede estar siendo utilizada por productos. Puedes desactivarla.");
        }
    }

    @FXML private void desactivarCategoria() {
        Categoria seleccionada = tablaCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) { alert(Alert.AlertType.WARNING, "Selecciona una categoría", "Selecciona una fila."); return; }
        try {
            categoriaDAO.desactivar(seleccionada.getId());
            cargarCategorias();
            cargarProductos();
        } catch (SQLException e) { error(e); }
    }

    @FXML private void guardarProducto() {
        try {
            Producto producto = leerProducto(false);
            productoDAO.guardar(producto);
            limpiarProducto();
            cargarProductos();
            info("Producto guardado", "El producto fue insertado usando PreparedStatement.");
        } catch (NumberFormatException e) {
            alert(Alert.AlertType.WARNING, "Datos numéricos", "Precio y existencia deben ser números válidos.");
        } catch (IllegalArgumentException | SQLException e) {
            error(e);
        }
    }

    @FXML private void actualizarProducto() {
        Producto seleccionada = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionada == null) { alert(Alert.AlertType.WARNING, "Selecciona un producto", "Selecciona una fila para actualizar."); return; }
        try {
            Producto producto = leerProducto(true);
            producto.setId(seleccionada.getId());
            productoDAO.actualizar(producto);
            cargarProductos();
            info("Producto actualizado", "Los cambios fueron guardados.");
        } catch (NumberFormatException e) { alert(Alert.AlertType.WARNING, "Datos numéricos", "Precio y existencia deben ser números válidos."); }
        catch (IllegalArgumentException | SQLException e) { error(e); }
    }

    @FXML private void eliminarProducto() {
        Producto seleccionada = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionada == null) { alert(Alert.AlertType.WARNING, "Selecciona un producto", "Selecciona una fila."); return; }
        try {
            productoDAO.eliminar(seleccionada.getId());
            limpiarProducto();
            cargarProductos();
        } catch (SQLException e) { error(e); }
    }

    @FXML private void desactivarProducto() {
        Producto seleccionada = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionada == null) { alert(Alert.AlertType.WARNING, "Selecciona un producto", "Selecciona una fila."); return; }
        try {
            productoDAO.desactivar(seleccionada.getId());
            cargarProductos();
        } catch (SQLException e) { error(e); }
    }

    @FXML private void limpiarCategoria() {
        txtCategoria.clear();
        chkCategoriaActiva.setSelected(true);
        tablaCategorias.getSelectionModel().clearSelection();
    }

    @FXML private void limpiarProducto() {
        txtCodigo.clear(); txtProducto.clear(); txtPrecio.clear(); txtExistencia.clear(); txtRutaImagen.clear();
        cbCategoria.getSelectionModel().clearSelection(); chkActivo.setSelected(true);
        tablaProductos.getSelectionModel().clearSelection();
    }

    private Producto leerProducto(boolean updating) {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtProducto.getText().trim();
        Categoria categoria = cbCategoria.getValue();

        int existencia = Integer.parseInt(txtExistencia.getText().trim());
        BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());

        if (codigo.isBlank() || nombre.isBlank() || categoria == null || txtPrecio.getText().isBlank() || txtExistencia.getText().isBlank()) {
            throw new IllegalArgumentException("Completa los campos obligatorios.");
        }

        if (existencia < 0) {
            throw new IllegalArgumentException("La existencia no puede ser un número negativo.");
        }
        if (precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser un número negativo.");
        }

        if (!updating && categoria.getId() == null) throw new IllegalArgumentException("La categoría no es válida.");
        return new Producto(null, codigo, nombre, categoria, new BigDecimal(txtPrecio.getText().trim()), Integer.parseInt(txtExistencia.getText().trim()),
                txtRutaImagen.getText().trim().isBlank() ? null : txtRutaImagen.getText().trim(), chkActivo.isSelected());
    }

    private void cargarCategoria(Categoria c) {
        if (c == null) return;
        txtCategoria.setText(c.getNombre());
        chkCategoriaActiva.setSelected(c.isActiva());
    }

    private void cargarProducto(Producto p) {
        if (p == null) return;
        txtCodigo.setText(p.getCodigo()); txtProducto.setText(p.getNombre()); cbCategoria.getSelectionModel().select(p.getCategoria());
        txtPrecio.setText(p.getPrecioVenta().toPlainString()); txtExistencia.setText(Integer.toString(p.getExistencia()));
        txtRutaImagen.setText(p.getRutaImagen() == null ? "" : p.getRutaImagen()); chkActivo.setSelected(p.isActivo());
    }

    private void cargarCategorias() {
        try {
            ObservableList<Categoria> data = FXCollections.observableArrayList(categoriaDAO.listar());
            tablaCategorias.setItems(data);
            cbCategoria.setItems(FXCollections.observableArrayList(data.stream().filter(Categoria::isActiva).toList()));
        } catch (SQLException e) { error(e); }
    }

    private void cargarProductos() {
        try { tablaProductos.setItems(FXCollections.observableArrayList(productoDAO.listar())); }
        catch (SQLException e) { error(e); }
    }

    private void alert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("Tienda JavaFX"); alert.setHeaderText(header); alert.setContentText(content); alert.showAndWait();
    }

    private void info(String header, String content) { alert(Alert.AlertType.INFORMATION, header, content); }

    private void error(Exception e) { alert(Alert.AlertType.ERROR, "Error", e.getMessage() == null ? e.toString() : e.getMessage()); }
}
