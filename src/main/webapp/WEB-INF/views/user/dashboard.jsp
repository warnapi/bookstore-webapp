<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Личный кабинет");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<h1 class="mb-4"><i class="bi bi-person-circle"></i> Личный кабинет</h1>

<div class="row">
    <div class="col-md-3">
        <div class="list-group">
            <a href="${pageContext.request.contextPath}/user/" class="list-group-item list-group-item-action active">
                <i class="bi bi-speedometer"></i> Главная
            </a>
            <a href="${pageContext.request.contextPath}/user/orders" class="list-group-item list-group-item-action">
                <i class="bi bi-receipt"></i> Мои заказы
            </a>
            <a href="${pageContext.request.contextPath}/user/profile" class="list-group-item list-group-item-action">
                <i class="bi bi-person-gear"></i> Профиль
            </a>
            <a href="${pageContext.request.contextPath}/review" class="list-group-item list-group-item-action">
                <i class="bi bi-chat"></i> Мои отзывы
            </a>
            <a href="${pageContext.request.contextPath}/wishlist/" class="list-group-item list-group-item-action">
                <i class="bi bi-heart"></i> Списки желаний
            </a>
        </div>
    </div>
    
    <div class="col-md-9">
        <div class="card">
            <div class="card-body text-center">
                <i class="bi bi-person-circle" style="font-size: 5rem; color: #667eea;"></i>
                <h3 class="mt-3">${sessionScope.user.name}</h3>
                <p class="text-muted">${sessionScope.user.email}</p>
                <p class="badge ${sessionScope.user.role.name() == 'ADMIN' ? 'bg-danger' : 'bg-primary'}">
                    ${sessionScope.user.role.name()}
                </p>
                
                <hr>
                
                <div class="row mt-4">
                    <div class="col-md-4">
                        <a href="${pageContext.request.contextPath}/user/orders" class="btn btn-outline-primary w-100">
                            <i class="bi bi-receipt"></i> Заказы
                        </a>
                    </div>
                    <div class="col-md-4">
                        <a href="${pageContext.request.contextPath}/user/profile" class="btn btn-outline-primary w-100">
                            <i class="bi bi-person-gear"></i> Профиль
                        </a>
                    </div>
                    <div class="col-md-4">
                        <a href="${pageContext.request.contextPath}/wishlist/" class="btn btn-outline-primary w-100">
                            <i class="bi bi-heart"></i> Списки
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
