<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Вход");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<div class="row justify-content-center">
    <div class="col-md-6 col-lg-5">
        <div class="card shadow">
            <div class="card-body p-4">
                <h2 class="text-center mb-4">Вход</h2>
                
                <form action="${pageContext.request.contextPath}/login" method="post">
                    <div class="mb-3">
                        <label for="email" class="form-label">Email</label>
                        <input type="email" class="form-control" id="email" name="email" required
                               value="${param.email}">
                    </div>
                    
                    <div class="mb-3">
                        <label for="password" class="form-label">Пароль</label>
                        <input type="password" class="form-control" id="password" name="password" required>
                    </div>
                    
                    <div class="mb-3 form-check">
                        <input type="checkbox" class="form-check-input" id="rememberMe" name="rememberMe">
                        <label class="form-check-label" for="rememberMe">Запомнить меня</label>
                    </div>
                    
                    <button type="submit" class="btn btn-primary w-100 mb-3">
                        Войти
                    </button>
                    
                    <div class="text-center">
                        <p class="mb-0">Нет аккаунта? <a href="${pageContext.request.contextPath}/register">Зарегистрироваться</a></p>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
