export function animateValue(start: number, end: number, duration: number, callback: (val: number)=>void){
  let startTime: any = null;
  function animate(timestamp: number) {
    if(!startTime) startTime = timestamp;
    const progress = Math.min((timestamp - startTime)/duration, 1);
    const value = start + (end - start) * progress;
    callback(Math.floor(value));
    if(progress<1) requestAnimationFrame(animate);
  }
  requestAnimationFrame(animate);
}
