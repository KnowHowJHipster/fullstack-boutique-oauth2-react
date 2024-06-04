import { IUser } from 'app/shared/model/user.model';
import { Gender } from 'app/shared/model/enumerations/gender.model';

export interface ICustomerDetails {
  id?: number;
  gender?: keyof typeof Gender;
  phone?: string;
  addressLine1?: string;
  addressLine2?: string | null;
  city?: string;
  country?: string;
  user?: IUser;
}

export const defaultValue: Readonly<ICustomerDetails> = {};
