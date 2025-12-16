export class OrderDetail {
    name !: string;
    price !: number;
    quantity !: number;
    
    // SubTotal là giá trị được tính toán, không cần gửi đến backend
    get subTotal(): number {
        return this.price * this.quantity;
    }

    constructor(name: string = '', price: number = 0, quantity: number = 0) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
}
