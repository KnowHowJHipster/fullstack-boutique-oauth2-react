import dayjs from 'dayjs';
import { ICustomerDetails } from 'app/shared/model/customer-details.model';
import { OrderStatus } from 'app/shared/model/enumerations/order-status.model';
import { PaymentMethod } from 'app/shared/model/enumerations/payment-method.model';

export interface IShoppingCart {
  id?: number;
  placedDate?: dayjs.Dayjs;
  status?: keyof typeof OrderStatus;
  totalPrice?: number;
  paymentMethod?: keyof typeof PaymentMethod;
  paymentReference?: string | null;
  customerDetails?: ICustomerDetails;
}

export const defaultValue: Readonly<IShoppingCart> = {};
