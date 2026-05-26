<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Корзина");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<h1 class="mb-4"><i class="bi bi-cart"></i> Корзина</h1>

<c:choose>
    <c:when test="${not empty cartItems and cartItems[0] != null}">
        <div class="row">
            <div class="col-lg-8">
                <div class="card">
                    <div class="card-body">
                        <c:forEach var="item" items="${cartItems}">
                            <div class="row border-bottom pb-3 mb-3 ${not item.isAvailable ? 'bg-light' : ''}">
                                <div class="col-md-2">
                                    <c:choose>
                                        <c:when test="${not empty item.bookImageUrl}">
                                            <img src="${item.bookImageUrl}" class="img-fluid" style="max-height: 100px;">
                                        </c:when>
                                        <c:otherwise>
                                            <div style="height: 100px; background: #f8f9fa; display: flex; align-items: center; justify-content: center;">
                                                <i class="bi bi-book fs-3 text-muted"></i>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="col-md-7">
                                    <h5>${item.bookTitle}</h5>
                                    <p class="text-muted mb-1">${item.bookAuthor}</p>
                                    <p class="price">${item.bookPrice} BYN</p>
                                    <c:if test="${!item.isAvailable}">
                                        <p class="text-danger mb-0">
                                            <i class="bi bi-exclamation-triangle"></i> Недостаточно на складе
                                        </p>
                                    </c:if>
                                </div>
                                <div class="col-md-3 text-end">
                                    <form action="${pageContext.request.contextPath}/cart" method="post" class="d-inline">
                                        <input type="hidden" name="action" value="update">
                                        <input type="hidden" name="bookId" value="${item.bookId}">
                                        <div class="input-group input-group-sm mb-2">
                                            <button type="submit" class="btn btn-outline-secondary" name="quantity" value="${item.quantity - 1}" ${item.quantity <= 1 ? 'disabled' : ''}>-</button>
                                            <input type="number" class="form-control text-center" value="${item.quantity}" min="1" max="${item.stockAvailable}">
                                            <button type="submit" class="btn btn-outline-secondary" name="quantity" value="${item.quantity + 1}" ${item.quantity >= item.stockAvailable ? 'disabled' : ''}>+</button>
                                        </div>
                                    </form>
                                    <form action="${pageContext.request.contextPath}/cart" method="post" class="d-inline">
                                        <input type="hidden" name="action" value="remove">
                                        <input type="hidden" name="bookId" value="${item.bookId}">
                                        <button type="submit" class="btn btn-sm btn-danger">
                                            <i class="bi bi-trash"></i>
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </div>
            
            <div class="col-lg-4">
                <div class="card">
                    <div class="card-body">
                        <h4>Итого к оплате</h4>
                        <hr>
                        <p class="display-6">${cartTotal} BYN</p>
                        
                        <c:if test="${hasUnavailableItems}">
                            <div class="alert alert-warning">
                                <i class="bi bi-exclamation-triangle"></i> Некоторые товары недоступны
                            </div>
                        </c:if>
                        
                        <a href="${pageContext.request.contextPath}/order" class="btn btn-primary btn-lg w-100">
                            Оформить заказ
                        </a>
                        
                        <a href="${pageContext.request.contextPath}/catalog" class="btn btn-outline-secondary w-100 mt-2">
                            Продолжить покупки
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <div class="text-center py-5">
            <i class="bi bi-cart-x fs-1 text-muted"></i>
            <h3 class="mt-3">Корзина пуста</h3>
            <p class="text-muted">Добавьте книги из каталога</p>
            <a href="${pageContext.request.contextPath}/catalog" class="btn btn-primary btn-lg">
                Перейти в каталог
            </a>
        </div>
    </c:otherwise>
</c:choose>
