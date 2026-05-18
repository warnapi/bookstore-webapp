<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Профиль");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<h1 class="mb-4"><i class="bi bi-person-gear"></i> Профиль</h1>

<div class="row">
    <div class="col-md-3">
        <div class="list-group">
            <a href="${pageContext.request.contextPath}/user/" class="list-group-item list-group-item-action">
                <i class="bi bi-speedometer"></i> Главная
            </a>
            <a href="${pageContext.request.contextPath}/user/orders" class="list-group-item list-group-item-action">
                <i class="bi bi-receipt"></i> Мои заказы
            </a>
            <a href="${pageContext.request.contextPath}/review" class="list-group-item list-group-item-action">
                <i class="bi bi-chat"></i> Мои отзывы
            </a>
            <a href="${pageContext.request.contextPath}/wishlist" class="list-group-item list-group-item-action">
                <i class="bi bi-heart"></i> Списки желаний
            </a>
            <a href="${pageContext.request.contextPath}/user/orders" class="list-group-item list-group-item-action">
                <i class="bi bi-receipt"></i> Мои заказы
            </a>
            <a href="${pageContext.request.contextPath}/user/profile" class="list-group-item list-group-item-action active">
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
        <!-- Обновление профиля -->
        <div class="card mb-4">
            <div class="card-header">
                <h5 class="mb-0">Обновить данные</h5>
            </div>
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/user" method="post">
                    <input type="hidden" name="action" value="updateProfile">
                    
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="name" class="form-label">Имя</label>
                            <input type="text" class="form-control" id="name" name="name" 
                                   value="${user.name}" required>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="email" class="form-label">Email</label>
                            <input type="email" class="form-control" id="email" name="email" 
                                   value="${user.email}" required>
                        </div>
                    </div>
                    
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-check-circle"></i> Сохранить
                    </button>
                </form>
            </div>
        </div>
        
        <!-- Смена пароля -->
        <div class="card">
            <div class="card-header">
                <h5 class="mb-0">Сменить пароль</h5>
            </div>
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/user" method="post">
                    <input type="hidden" name="action" value="changePassword">
                    
                    <div class="mb-3">
                        <label for="oldPassword" class="form-label">Текущий пароль</label>
                        <input type="password" class="form-control" id="oldPassword" name="oldPassword" required>
                    </div>
                    
                    <div class="mb-3">
                        <label for="newPassword" class="form-label">Новый пароль</label>
                        <input type="password" class="form-control" id="newPassword" name="newPassword" 
                               minlength="6" required>
                    </div>
                    
                    <div class="mb-3">
                        <label for="confirmPassword" class="form-label">Подтвердите новый пароль</label>
                        <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required>
                    </div>
                    
                    <button type="submit" class="btn btn-warning">
                        <i class="bi bi-lock"></i> Сменить пароль
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>
