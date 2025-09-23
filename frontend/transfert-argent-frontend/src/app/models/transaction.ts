import { Compte } from './compte';
export interface Transaction {
  id: number;
  montant: number;
  frais: number;
  statut: string;
  dateTransaction: Date;
  compteSource: Compte;
  compteDestination: Compte;
  receipt?: any; // Optionnel
}