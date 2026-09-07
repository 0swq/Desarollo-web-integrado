package com.autopartes;

import com.autopartes.model.*;

import java.util.ArrayList;

public class DataStore {

    private static final DataStore INSTANCIA = new DataStore();

    private ArrayList<Usuario> usuarios = new ArrayList<>();
    private ArrayList<Categoria> categorias = new ArrayList<>();
    private ArrayList<ProductoCategoria> productoCategorias = new ArrayList<>();
    private ArrayList<Proveedor> proveedores = new ArrayList<>();
    private ArrayList<Producto> productos = new ArrayList<>();
    private ArrayList<MarcaVehiculo> marcasVehiculos = new ArrayList<>();
    private ArrayList<ModeloVehiculo> modelosVehiculos = new ArrayList<>();
    private ArrayList<CompatibilidadVehiculo> compatibilidadesVehiculos = new ArrayList<>();
    private ArrayList<Stock> stocks = new ArrayList<>();
    private ArrayList<MovimientoStock> movimientosStock = new ArrayList<>();
    private ArrayList<Carrito> carritos = new ArrayList<>();
    private ArrayList<ItemCarrito> itemsCarrito = new ArrayList<>();
    private ArrayList<Orden> ordenes = new ArrayList<>();
    private ArrayList<ItemOrden> itemsOrden = new ArrayList<>();
    private ArrayList<Parametro> parametros = new ArrayList<>();
    private ArrayList<Pago> pagos = new ArrayList<>();

    private DataStore() {}

    public static DataStore obtenerInstancia() {
        return INSTANCIA;
    }

    public void setUsuarios(ArrayList<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public void setCategorias(ArrayList<Categoria> categorias) {
        this.categorias = categorias;
    }

    public void setProductoCategorias(ArrayList<ProductoCategoria> productoCategorias) {
        this.productoCategorias = productoCategorias;
    }

    public void setProveedores(ArrayList<Proveedor> proveedores) {
        this.proveedores = proveedores;
    }

    public void setProductos(ArrayList<Producto> productos) {
        this.productos = productos;
    }

    public void setMarcasVehiculos(ArrayList<MarcaVehiculo> marcasVehiculos) {
        this.marcasVehiculos = marcasVehiculos;
    }

    public void setModelosVehiculos(ArrayList<ModeloVehiculo> modelosVehiculos) {
        this.modelosVehiculos = modelosVehiculos;
    }

    public void setCompatibilidadesVehiculos(ArrayList<CompatibilidadVehiculo> compatibilidadesVehiculos) {
        this.compatibilidadesVehiculos = compatibilidadesVehiculos;
    }

    public void setStocks(ArrayList<Stock> stocks) {
        this.stocks = stocks;
    }

    public void setMovimientosStock(ArrayList<MovimientoStock> movimientosStock) {
        this.movimientosStock = movimientosStock;
    }

    public void setCarritos(ArrayList<Carrito> carritos) {
        this.carritos = carritos;
    }

    public void setItemsCarrito(ArrayList<ItemCarrito> itemsCarrito) {
        this.itemsCarrito = itemsCarrito;
    }

    public void setOrdenes(ArrayList<Orden> ordenes) {
        this.ordenes = ordenes;
    }

    public void setItemsOrden(ArrayList<ItemOrden> itemsOrden) {
        this.itemsOrden = itemsOrden;
    }

    public void setParametros(ArrayList<Parametro> parametros) {
        this.parametros = parametros;
    }

    public void setPagos(ArrayList<Pago> pagos) {
        this.pagos = pagos;
    }

    public ArrayList<Usuario> getUsuarios() {
        return usuarios;
    }

    public ArrayList<Categoria> getCategorias() {
        return categorias;
    }

    public ArrayList<ProductoCategoria> getProductoCategorias() {
        return productoCategorias;
    }

    public ArrayList<Proveedor> getProveedores() {
        return proveedores;
    }

    public ArrayList<Producto> getProductos() {
        return productos;
    }

    public ArrayList<MarcaVehiculo> getMarcasVehiculos() {
        return marcasVehiculos;
    }

    public ArrayList<ModeloVehiculo> getModelosVehiculos() {
        return modelosVehiculos;
    }

    public ArrayList<CompatibilidadVehiculo> getCompatibilidadesVehiculos() {
        return compatibilidadesVehiculos;
    }

    public ArrayList<Stock> getStocks() {
        return stocks;
    }

    public ArrayList<MovimientoStock> getMovimientosStock() {
        return movimientosStock;
    }

    public ArrayList<Carrito> getCarritos() {
        return carritos;
    }

    public ArrayList<ItemCarrito> getItemsCarrito() {
        return itemsCarrito;
    }

    public ArrayList<Orden> getOrdenes() {
        return ordenes;
    }

    public ArrayList<ItemOrden> getItemsOrden() {
        return itemsOrden;
    }

    public ArrayList<Parametro> getParametros() {
        return parametros;
    }

    public ArrayList<Pago> getPagos() {
        return pagos;
    }
}