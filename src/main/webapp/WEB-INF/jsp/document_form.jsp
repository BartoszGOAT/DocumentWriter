<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>New Document - Collaborative Editor</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.9.3/css/bulma.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <!-- Navigation Bar -->
    <nav class="navbar is-primary" role="navigation" aria-label="main navigation">
        <div class="navbar-brand">
            <a class="navbar-item" href="${pageContext.request.contextPath}/">
                <strong>CollabEditor</strong>
            </a>
            
            <a role="button" class="navbar-burger" aria-label="menu" aria-expanded="false" data-target="navbarBasic">
                <span aria-hidden="true"></span>
                <span aria-hidden="true"></span>
                <span aria-hidden="true"></span>
            </a>
        </div>
        
        <div id="navbarBasic" class="navbar-menu">
            <div class="navbar-start">
                <a class="navbar-item" href="${pageContext.request.contextPath}/dashboard">
                    Dashboard
                </a>
                
                <a class="navbar-item is-active" href="${pageContext.request.contextPath}/documents/new">
                    New Document
                </a>
            </div>
            
            <div class="navbar-end">
                <div class="navbar-item">
                    <p>Hello, <strong>${sessionScope.username}</strong></p>
                </div>
                <div class="navbar-item">
                    <div class="buttons">
                        <a class="button is-light" href="${pageContext.request.contextPath}/auth/logout">
                            Log out
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </nav>

    <!-- Main Content -->
    <section class="section">
        <div class="container">
            <h1 class="title">Create New Document</h1>
            
            <div class="box">
                <c:if test="${not empty error}">
                    <div class="notification is-danger">
                        ${error}
                    </div>
                </c:if>
                
                <form action="${pageContext.request.contextPath}/documents" method="post">
                    <div class="field">
                        <label class="label">Document Title</label>
                        <div class="control">
                            <input class="input" type="text" name="title" placeholder="Enter a title for your document" required>
                        </div>
                    </div>
                    
                    <div class="field">
                        <label class="label">Initial Content (Optional)</label>
                        <div class="control">
                            <textarea class="textarea" name="content" placeholder="Enter initial content for your document" rows="10"></textarea>
                        </div>
                    </div>
                    
                    <div class="field">
                        <div class="control">
                            <button type="submit" class="button is-primary">Create Document</button>
                            <a href="${pageContext.request.contextPath}/dashboard" class="button is-light">Cancel</a>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </section>

    <footer class="footer">
        <div class="content has-text-centered">
            <p>
                <strong>Collaborative Document Editor</strong> by L3 Informatique.
            </p>
        </div>
    </footer>

    <script>
        // Mobile menu toggle
        document.addEventListener('DOMContentLoaded', () => {
            const navbarBurgers = Array.prototype.slice.call(document.querySelectorAll('.navbar-burger'), 0);
            if (navbarBurgers.length > 0) {
                navbarBurgers.forEach(el => {
                    el.addEventListener('click', () => {
                        const target = document.getElementById(el.dataset.target);
                        el.classList.toggle('is-active');
                        target.classList.toggle('is-active');
                    });
                });
            }
        });
    </script>
</body>
</html>