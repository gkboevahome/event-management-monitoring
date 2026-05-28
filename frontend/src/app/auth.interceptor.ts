import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Вземаме токена от localStorage
  const token = localStorage.getItem('access_token');

  // Ако токенът съществува, клонираме заявката и добавяме Authorization заглавката
  if (token) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(clonedRequest);
  }

  // Ако няма токен, изпращаме оригиналната заявка без промени
  return next(req);
};
