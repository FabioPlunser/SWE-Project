import {
  type Renderable,
  type DefaultToastOptions,
  type ToastOptions,
  type ValueOrFunction,
} from "./types";
type Message = Renderable;
type ToastHandler = (message: Message, options?: ToastOptions) => string;
declare const toast: {
  (message: Message, opts?: ToastOptions): string;
  error: ToastHandler;
  success: ToastHandler;
  loading: ToastHandler;
  custom: ToastHandler;
  dismiss(toastId?: string): void;
  remove(toastId?: string): void;
  promise<T>(
    promise: Promise<T>,
    msgs: {
      loading: Renderable;
      success: ValueOrFunction<Renderable, T>;
      error: ValueOrFunction<Renderable, any>;
    },
    opts?: DefaultToastOptions
  ): Promise<T>;
};
export default toast;
