 const stripe = Stripe('pk_test_51QDKjnHp1iv0Lmvp6Q3BhIOsp97QkAdHTP3YFZDYOYxRWqD8dM4AKDvrQeQXaimcBcUqdTeV2yEivRhPuOicGZct00uESgjun9');
 const paymentButton = document.querySelector('#paymentButton');
 
 paymentButton.addEventListener('click', () => {
   stripe.redirectToCheckout({
     sessionId: sessionId
   })
 });
