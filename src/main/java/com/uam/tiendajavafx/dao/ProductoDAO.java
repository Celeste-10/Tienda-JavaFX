package com.uam.tiendajavafx.dao;

import com.uam.tiendajavafx.model.Categoria;
import com.uam.tiendajavafx.model.Producto;
import com.uam.tiendajavafx.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO para Producto usando PreparedStatement e INNER JOIN. */
public class ProductoDAO {
    private static final String SELECT_JOIN = """
        SELECT p.id, p.codigo, p.nombre, p.categoria_id, c.nombre AS categoria_nombre,
               p.precio_venta, p.existencia, p.ruta_imagen, p.activo
        FROM producto p
        INNER JOIN categoria c ON p.categoria_id = c.id
        """;

    public void guardar(Producto producto) throws SQLException {
        String sql = """
            INSERT INTO producto
            (codigo, nombre, categoria_id, precio_venta, existencia, ruta_imagen, activo)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getRutaImagen());
            ps.setBoolean(7, producto.isActivo());
            ps.executeUpdate();
        }
    }

    public List<Producto> listar() throws SQLException {
        String sql = SELECT_JOIN + " ORDER BY p.id";
        List<Producto> lista = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    public Producto buscar(int id) throws SQLException {
        String sql = SELECT_JOIN + " WHERE p.id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public void actualizar(Producto producto) throws SQLException {
        String sql = """
            UPDATE producto
            SET codigo = ?, nombre = ?, categoria_id = ?, precio_venta = ?,
                existencia = ?, ruta_imagen = ?, activo = ?
            WHERE id = ?
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getRutaImagen());
            ps.setBoolean(7, producto.isActivo());
            ps.setInt(8, producto.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void desactivar(int id) throws SQLException {
        String sql = "UPDATE producto SET activo = FALSE WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Producto mapRow(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(
            rs.getInt("categoria_id"),
            rs.getString("categoria_nombre"),
            true
        );
        return new Producto(
            rs.getInt("id"), rs.getString("codigo"), rs.getString("nombre"), categoria,
            rs.getBigDecimal("precio_venta"), rs.getInt("existencia"),
            rs.getString("ruta_imagen"), rs.getBoolean("activo")
        );
    }
}
