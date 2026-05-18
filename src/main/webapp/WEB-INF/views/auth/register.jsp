<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Регистрация");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<div class="row justify-content-center">
    <div class="col-md-6 col-lg-5">
        <div class="card shadow">
            <div class="card-body p-4">
                <h2 class="text-center mb-4">Регистрация</h2>
                
                <form action="${pageContext.request.contextPath}/register" method="post">
                    <div class="mb-3">
                        <label for="name" class="form-label">Имя</label>
                        <input type="text" class="form-control" id="name" name="name" required
                               value="${param.name}">
                    </div>
                    
                    <div class="mb-3">
                        <label for="email" class="form-label">Email</label>
                        <input type="email" class="form-control" id="email" name="email" required
                               value="${param.email}">
                    </div>
                    
                    <div class="mb-3">
                        <label for="password" class="form-label">Пароль</label>
                        <input type="password" class="form-control" id="password" name="password" 
                               minlength="6" required>
                        <div class="form-text">Минимум 6 символов</div>
                    </div>
                    
                    <div class="mb-3">
                        <label for="confirmPassword" class="form-label">Повторите пароль</label>
                        <input type="password" class="form-control" id="confirmPassword" 
                               name="confirmPassword" required>
                    </div>
                    
                    <button type="submit" class="btn btn-primary w-100 mb-3">
                        Зарегистрироваться
                    </button>
                    
                    <div class="text-center">
                        <p class="mb-0">Уже есть аккаунт? <a href="${pageContext.request.contextPath}/login">Войти</a></p>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
