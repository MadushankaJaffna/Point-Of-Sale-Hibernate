package io.github.madushanka.pos.business.custom.impl;


import io.github.madushanka.pos.business.custom.OrderBO;
import io.github.madushanka.pos.dao.DAOFactory;
import io.github.madushanka.pos.dao.DAOTypes;
import io.github.madushanka.pos.dao.custom.*;
import io.github.madushanka.pos.db.DBConnection;
import io.github.madushanka.pos.db.HibernateUtill;
import io.github.madushanka.pos.dto.OrderDTO;
import io.github.madushanka.pos.dto.OrderDTO2;
import io.github.madushanka.pos.dto.OrderDetailDTO;
import io.github.madushanka.pos.entity.CustomEntity;
import io.github.madushanka.pos.entity.Item;
import io.github.madushanka.pos.entity.Order;
import io.github.madushanka.pos.entity.OrderDetail;
import org.hibernate.Session;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderBOImpl implements OrderBO {

    private OrderDAO orderDAO = DAOFactory.getInstance().getDAO(DAOTypes.ORDER);
    private OrderDetailDAO orderDetailDAO = DAOFactory.getInstance().getDAO(DAOTypes.ORDER_DETAIL);
    private ItemDAO itemDAO = DAOFactory.getInstance().getDAO(DAOTypes.ITEM);
    private QueryDAO queryDAO = DAOFactory.getInstance().getDAO(DAOTypes.QUERY);
    private CustomerDAO customerDAO = DAOFactory.getInstance().getDAO(DAOTypes.CUSTOMER);

    @Override
    public int getLastOrderId() throws Exception {
        try(Session session = HibernateUtill.getSessionFactory().openSession()){
            orderDAO.setSession(session);
            session.beginTransaction();
            int lastOrderId = orderDAO.getLastOrderId();
            session.getTransaction().commit();
            return lastOrderId;
        }

    }

    @Override
    public void placeOrder(OrderDTO order) throws Exception {

        try (Session session = HibernateUtill.getSessionFactory().openSession()){
            orderDAO.setSession(session);
            customerDAO.setSession(session);
            orderDetailDAO.setSession(session);
            itemDAO.setSession(session);

            session.beginTransaction();

            int oId = order.getId();
            orderDAO.save(new Order(oId, new java.sql.Date(new Date().getTime()),customerDAO.find(order.getCustomerId())));

            for (OrderDetailDTO orderDetail : order.getOrderDetails()) {
                orderDetailDAO.save(new OrderDetail(oId, orderDetail.getCode(),
                        orderDetail.getQty(), orderDetail.getUnitPrice()));

                Item item = itemDAO.find(orderDetail.getCode());
                item.setQtyOnHand(item.getQtyOnHand() - orderDetail.getQty());
                itemDAO.update(item);

            }
            session.getTransaction().commit();
        }
    }

    @Override
    public List<OrderDTO2> getOrderInfo(String query) throws Exception {
        try(Session session = HibernateUtill.getSessionFactory().openSession()){
            queryDAO.setSession(session);
            session.beginTransaction();
            List<CustomEntity> ordersInfo = queryDAO.getOrdersInfo(query + "%");
            List<OrderDTO2> dtos = new ArrayList<>();
            for (CustomEntity info : ordersInfo) {
                dtos.add(new OrderDTO2(info.getOrderId(),
                        info.getOrderDate(),info.getCustomerId(),info.getCustomerName(),info.getOrderTotal()));
            }

            session.getTransaction().commit();
            return dtos;
        }
    }
}
