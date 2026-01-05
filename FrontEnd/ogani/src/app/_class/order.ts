export class Order {

    id!: number;
    firstname !: string;
    lastname !: string;
    country !: string;
    address!: string;
    town!: string;
    state!: string;
    postCode !: number
    email!: string;
    phone!: string;
    note!: string;
    totalPrice !: number;
    createdDate!: Date;
    paymentMethod!: string;
    status?: string;
    orderDetails?: any[];
    username?: string;
    
}
