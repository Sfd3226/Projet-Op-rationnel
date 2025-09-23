// transaction-dto.ts
export interface TransactionDTO {
  id: number;
  montant: number;
  frais: number;
  statut: string;
  dateTransaction: Date;
  compteSourceNumero: string;
  compteDestinationNumero: string;
  type: 'ENVOI' | 'RECEPTION';
  receiptNumero?: string; 
}